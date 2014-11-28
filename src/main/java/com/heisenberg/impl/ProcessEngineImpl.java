/*
 * Copyright 2014 Heisenberg Enterprises Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.heisenberg.impl;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ActivityInstanceQuery;
import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.NotifyActivityInstanceRequest;
import com.heisenberg.api.Page;
import com.heisenberg.api.ParseIssues;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.VariableRequest;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.activities.bpmn.EmbeddedSubprocess;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.ScriptTask;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.TextType;
import com.heisenberg.api.util.Plugin;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionValidator;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.impl.json.Json;
import com.heisenberg.impl.script.ScriptRunnerImpl;
import com.heisenberg.impl.util.Exceptions;
import com.heisenberg.impl.util.Reflection;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl implements ProcessEngine {

  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public String id;
  
  /** activityDescriptors describe configuration fields for user defined activity types.
   * These will be sent to the process builder where users will be able to configure the activities. */
  public Map<String,ActivityTypeDescriptor> activityTypeDescriptorsByTypeId;
  public Map<Class<?>,ActivityTypeDescriptor> activityTypeDescriptorsByClass;

  /** types describe the configuration fields for user defined data types.
   * Will be sent to the process builder where users will be able to configure the concrete types. */
  public Map<String,DataTypeDescriptor> dataTypeDescriptorsByTypeId;
  public Map<Class<?>,DataTypeDescriptor> dataTypeDescriptorsByClass;
  
  public Executor executor;
  public Json json;
  public ProcessDefinitionCache processDefinitionCache;
  public ScriptRunnerImpl scriptRunner;
  
  protected ProcessEngineImpl() {
  }

  protected void initializeDefaults() {
    initializeDefaultId();
    initializeDefaultExecutor();
    initializeDefaultProcessDefinitionCache();
    initializeDefaultJson();
    initializeDefaultScriptRunner();
    initializeDefaultPluggableImplementations();
    initializeDefaultPlugins();
  }

  protected void initializeDefaultProcessDefinitionCache() {
    this.processDefinitionCache = new SimpleProcessDefinitionCache();
  }

  /** The globally unique id for this process engine used for locking 
   * process instances and jobs.  
   * This default implementation initializes the id of this process engine to "ipaddress:pid" */
  protected void initializeDefaultId() {
    try {
      id = InetAddress.getLocalHost().getHostAddress();
      String processName = ManagementFactory.getRuntimeMXBean().getName();
      int atIndex = processName.indexOf('@');
      if (atIndex>0) {
        id+=":"+processName.substring(0,atIndex);
      }
    } catch (UnknownHostException e) {
      id = "amnesia";
    }
  }
  
  protected void initializeDefaultPluggableImplementations() {
    activityTypeDescriptorsByTypeId = new HashMap<>();
    activityTypeDescriptorsByClass = new HashMap<>();
    dataTypeDescriptorsByTypeId = new HashMap<>();
    dataTypeDescriptorsByClass = new HashMap<>();
    Iterator<Plugin> spis = ServiceLoader.load(Plugin.class).iterator();
    while (spis.hasNext()) {
      Plugin spiObject = spis.next();
      registerPlugin(spiObject);
    }
  }
  
  protected void initializeDefaultPlugins() {
    registerDataType(TextType.INSTANCE);
    registerActivityType(StartEvent.INSTANCE);
    registerActivityType(EndEvent.INSTANCE);
    registerActivityType(new ScriptTask());
    registerActivityType(new UserTask());
    registerActivityType(EmbeddedSubprocess.INSTANCE);
  }

  public ProcessEngineImpl registerJavaBeanType(Class<?> javaBeanClass) {
    JavaBeanType javaBeanType = new JavaBeanType(javaBeanClass);
    javaBeanType.processEngine = this;
    registerPlugin(javaBeanType);
    return this;
  }

  public ProcessEngineImpl registerType(Class<? extends DataType> typeClass) {
    registerPlugin(Reflection.newInstance(typeClass));
    return this;
  }

  public ProcessEngineImpl registerType(DataType dataType) {
    registerPlugin(dataType);
    return this;
  }

  public ProcessEngineImpl registerActivityType(Class<? extends ActivityType> activityTypeClass) {
    registerPlugin(Reflection.newInstance(activityTypeClass));
    return this;
  }

  PluginDescriptor registerPlugin(Plugin pluginObject) {
    if (pluginObject==null) {
      throw new RuntimeException("Can't register null as a plugin");
    }
    String pluginTypeId = pluginObject.getTypeId();
    if (pluginTypeId==null) {
      throw new RuntimeException("Invalid registration of pluggable class: "+pluginObject.getClass().getName()+".getTypeId() does not return a value");
    }
    Class<?> pluginClass = pluginObject.getClass();
    if (DataType.class.isAssignableFrom(pluginClass)) {
      return registerDataType((DataType) pluginObject);
    } else if (ActivityType.class.isAssignableFrom(pluginClass)) {
      return registerActivityType((ActivityType) pluginObject);
    } else {
      throw new RuntimeException("Unknown plugin type: "+pluginClass.getName());
    }
  }

  protected PluginDescriptor registerActivityType(ActivityType pluginObject) {
    json.registerSubtype(pluginObject.getClass());
    ActivityTypeDescriptor activityTypeDescriptor = new ActivityTypeDescriptor(this, pluginObject);
    registerActivityTypeDescriptor(activityTypeDescriptor);
    return activityTypeDescriptor;
  }

  protected PluginDescriptor registerDataType(DataType pluginObject) {
    json.registerSubtype(pluginObject.getClass());
    DataTypeDescriptor dataTypeDescriptor = new DataTypeDescriptor(this, pluginObject);
    registerDataTypeDescriptor(dataTypeDescriptor);
    return dataTypeDescriptor;
  }
  
  public void registerDataTypeDescriptor(DataTypeDescriptor dataTypeDescriptor) {
    Class<?> dataTypeClass = dataTypeDescriptor.pluginClass;
    String dataTypeTypeId = dataTypeDescriptor.typeId;
    if (!dataTypeDescriptorsByClass.containsKey(dataTypeClass)) {
      PluginDescriptor existingPluginDescriptor = dataTypeDescriptorsByTypeId.get(dataTypeTypeId);
      if (existingPluginDescriptor!=null) {
        throw new RuntimeException("Duplicate DataType typeId: "+dataTypeTypeId+": "+dataTypeClass.getName()+" and "+existingPluginDescriptor.pluginClass.getName());
      }
      dataTypeDescriptorsByTypeId.put(dataTypeTypeId, dataTypeDescriptor);
      dataTypeDescriptorsByClass.put(dataTypeClass, dataTypeDescriptor);
    } 
  }

  public void registerActivityTypeDescriptor(ActivityTypeDescriptor activityTypeDescriptor) {
    Class<?> activityTypeClass = activityTypeDescriptor.pluginClass;
    String activityTypeTypeId = activityTypeDescriptor.typeId;
    if (!activityTypeDescriptorsByClass.containsKey(activityTypeClass)) {
      PluginDescriptor existingPluginDescriptor = activityTypeDescriptorsByTypeId.get(activityTypeTypeId);
      if (existingPluginDescriptor!=null) {
        throw new RuntimeException("Duplicate ActivityType typeId: "+activityTypeTypeId+": "+activityTypeClass.getName()+" and "+existingPluginDescriptor.pluginClass.getName());
      }
      activityTypeDescriptorsByTypeId.put(activityTypeTypeId, activityTypeDescriptor);
      activityTypeDescriptorsByClass.put(activityTypeClass, activityTypeDescriptor);
    } 
  }

  protected void initializeDefaultExecutor() {
    // TODO apply these tips: http://java.dzone.com/articles/executorservice-10-tips-and
    this.executor = new ScheduledThreadPoolExecutor(4, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  protected void initializeDefaultJson() {
    this.json = new Json(this);
  }
  
  protected void initializeDefaultScriptRunner() {
    this.scriptRunner = new ScriptRunnerImpl();
  }

  /// Process Definition Builder 
  
  @Override
  public ProcessBuilder newProcess() {
    ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl();
    processDefinition.processDefinition = processDefinition;
    processDefinition.processEngine = this;
    return processDefinition;
  }

  @Override
  public DeployProcessDefinitionResponse deployProcessDefinition(ProcessBuilder processBuilder) {
    Exceptions.checkNotNull(processBuilder, "processBuilder");

    DeployProcessDefinitionResponse response = new DeployProcessDefinitionResponse();

    ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) processBuilder;
    ProcessDefinitionValidator validator = new ProcessDefinitionValidator(this);
    processDefinition.visit(validator);
    ParseIssues issues = validator.getIssues();
    
    if (!issues.hasErrors()) {
      processDefinition.id = createProcessDefinitionId(processDefinition);
      response.setProcessDefinitionId(processDefinition.id); 
      insertProcessDefinition(processDefinition);
      processDefinitionCache.put(processDefinition);
    } else {
      response.setIssues(issues);
    }
    
    return response;
  }
  
  /** ensures that every element in this process definition has an id */
  protected Object createProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return UUID.randomUUID().toString();
  }
  
  /** @param processDefinition is a validated process definition that has no errors.  It might have warnings. */
  protected abstract void insertProcessDefinition(ProcessDefinitionImpl processDefinition);

  public ProcessInstance startProcessInstance(StartProcessInstanceRequest startProcessInstanceRequest) {
    Object processDefinitionId = startProcessInstanceRequest.processDefinitionId;
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinitionImpl processDefinition = findProcessDefinitionByIdUsingCache(processDefinitionId);
    if (processDefinition==null) {
      throw new RuntimeException("Could not find process definition "+processDefinitionId);
    }
    ProcessInstanceImpl processInstance = createProcessInstance(processDefinition);
    processInstance.transientContext = startProcessInstanceRequest.transientContext;
    setVariableApiValues(processInstance, startProcessInstanceRequest);
      
    log.debug("Starting "+processInstance);
    processInstance.setStart(Time.now());
    List<ActivityDefinition> startActivityDefinitions = processDefinition.getStartActivities();
    if (startActivityDefinitions!=null) {
      for (ActivityDefinition startActivityDefinition: startActivityDefinitions) {
        processInstance.start(startActivityDefinition);
      }
    }
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    processInstance.setLock(lock);
    insertProcessInstance(processInstance);
    processInstance.executeOperations();
    return processInstance;
  }

  private void setVariableApiValues(ScopeInstanceImpl scopeInstance, VariableRequest variableRequest) {
    ProcessDefinitionImpl processDefinition = scopeInstance.processDefinition;
    Map<Object, Object> variableValuesJson = variableRequest.variableValuesJson;
    // If there are variables in json format
    if (variableValuesJson!=null) {
      Map<Object,Object> internalValues = new LinkedHashMap<>();
      for (Object variableDefinitionId: variableValuesJson.keySet()) {
        Object jsonValue = variableValuesJson.get(variableDefinitionId);
        VariableDefinitionImpl variableDefinition = processDefinition.findVariableDefinition(variableDefinitionId);
        Object internalValue = variableDefinition.dataType.convertJsonToInternalValue(jsonValue);
        internalValues.put(variableDefinitionId, internalValue);
      }
      scopeInstance.setVariableValuesRecursive(internalValues);
    } 
    Map<Object, Object> variableValues = variableRequest.variableValues;
    if (variableValues!=null) {
      for (Object variableDefinitionId: variableValues.keySet()) {
        Object internalValue = variableValues.get(variableDefinitionId);
        VariableDefinitionImpl variableDefinition = processDefinition.findVariableDefinition(variableDefinitionId);
        variableDefinition.dataType.validateInternalValue(internalValue);
      }
    }
    if (variableValues!=null) {
      scopeInstance.setVariableValuesRecursive(variableValues);
    }
  }

  public ProcessDefinitionImpl findProcessDefinitionByIdUsingCache(Object processDefinitionId) {
    ProcessDefinitionImpl processDefinition = processDefinitionCache.get(processDefinitionId);
    if (processDefinition==null) {
      processDefinition = loadProcessDefinitionById(processDefinitionId);
      processDefinitionCache.put(processDefinition);
    }
    return processDefinition;
  }
  
  protected abstract ProcessDefinitionImpl loadProcessDefinitionById(Object processDefinitionId);

  protected ProcessInstanceImpl createProcessInstance(ProcessDefinitionImpl processDefinition) {
    return new ProcessInstanceImpl(this, processDefinition, createProcessInstanceId(processDefinition));
  }

  protected Object createProcessInstanceId(ProcessDefinitionImpl processDefinition) {
    return UUID.randomUUID();
  }
  
  /** instantiates and assign an id.
   * parent and activityDefinition are only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the parent or the activityDefinition. */
  public ActivityInstanceImpl createActivityInstance(ScopeInstanceImpl parent, ActivityDefinitionImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = createActivityInstanceId();
    return activityInstance;
  }

  protected Object createActivityInstanceId() {
    return UUID.randomUUID();
  }

  /** instantiates and assign an id.
   * parent and variableDefinition are only passed for reference.  
   * Apart from choosing the variable instance class to instantiate and assigning the id,
   * this method does not need to link the parent or variableDefinition. */
  public VariableInstanceImpl createVariableInstance(ScopeInstanceImpl parent, VariableDefinitionImpl variableDefinition) {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.id = createVariableInstanceId();
    return variableInstance;
  }

  protected Object createVariableInstanceId() {
    return UUID.randomUUID();
  }

  @Override
  public ProcessInstance notifyActivityInstance(NotifyActivityInstanceRequest notifyActivityInstanceRequest) {
    Object activityInstanceId = notifyActivityInstanceRequest.getActivityInstanceId();
    ProcessInstanceImpl processInstance = lockProcessInstanceByActivityInstanceId(activityInstanceId);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(activityInstanceId);
    if (activityInstance.isEnded()) {
      throw new RuntimeException("Activity instance "+activityInstance+" is already ended");
    }
    log.debug("Signalling "+activityInstance);
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.activityType.notify(activityInstance);
    processInstance.executeOperations();
    return processInstance;
  }
  
  public abstract ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(Object activityInstanceId);

  public abstract void insertProcessInstance(ProcessInstanceImpl processInstance);

  public abstract void flush(ProcessInstanceImpl processInstance);

  public abstract void flushAndUnlock(ProcessInstanceImpl processInstance);
  
  @Override
  public ActivityInstanceQuery createActivityInstanceQuery() {
    return new ActivityInstanceQueryImpl(this);
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public Executor getExecutor() {
    return executor;
  }
  
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }
  
  public Json getJson() {
    return json;
  }
  
  public void setJson(Json json) {
    this.json = json;
  }
  
  public ScriptRunnerImpl getScriptRunner() {
    return scriptRunner;
  }
  
  public void setScriptRunner(ScriptRunnerImpl scriptRunner) {
    this.scriptRunner = scriptRunner;
  }

  public abstract List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQuery processInstanceQuery);

  public abstract List<ProcessDefinitionImpl> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery);

  public abstract Page<ActivityInstance> findActivityInstances(ActivityInstanceQueryImpl activityInstanceQueryImpl);

  public ActivityTypeDescriptor findActivityDescriptorByClass(Class<?> activityTypeClass) {
    return activityTypeDescriptorsByClass.get(activityTypeClass);
  }

  public ActivityTypeDescriptor findActivityDescriptorByTypeId(String activityTypeTypeId) {
    return activityTypeDescriptorsByTypeId.get(activityTypeTypeId);
  }

  public DataTypeDescriptor findDataTypeDescriptorByClass(Class<?> activityTypeClass) {
    return dataTypeDescriptorsByClass.get(activityTypeClass);
  }

  public DataTypeDescriptor findDataTypeDescriptorByTypeId(String activityTypeTypeId) {
    return dataTypeDescriptorsByTypeId.get(activityTypeTypeId);
  }
}

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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
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
import com.heisenberg.api.Page;
import com.heisenberg.api.ParseIssues;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.SignalRequest;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.id.ActivityInstanceId;
import com.heisenberg.api.id.ProcessDefinitionId;
import com.heisenberg.api.id.ProcessInstanceId;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.type.TextType;
import com.heisenberg.bpmn.activities.StartEvent;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.ProcessValidator;
import com.heisenberg.definition.VariableDefinitionImpl;
import com.heisenberg.expressions.ScriptRunnerImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.LockImpl;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.instance.ScopeInstanceImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.DataType;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Spi;
import com.heisenberg.type.JavaBeanType;
import com.heisenberg.util.Exceptions;
import com.heisenberg.util.Reflection;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl implements ProcessEngine {

  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public String id;
  
  /** defaultActivityTypes are the configured default activity types.
   * Will not be synchronized with the process builder. */
  public Map<String,ActivityType> defaultActivityTypes;

  /** activityTypes are the configured user defined activity types.
   * Will be synchronized with the process builder. */
  public Map<String,ActivityType> activityTypes;
  
  /** activityDescriptors describe configuration fields for user defined activity types.
   * These will be sent to the process builder where users will be able to configure the activities. */
  public Map<String,SpiDescriptor> activityDescriptors;

  /** defaultTypes are the configured default data types.
   * Will not be sent to the process builder. */
  public Map<String,DataType> defaultTypes;

  /** types are the configured user defined data types.
   * Will be sent to the process builder. */
  public Map<String,DataType> dataTypes;
  
  /** types describe the configuration fields for user defined data types.
   * Will be sent to the process builder where users will be able to configure the concrete types. */
  public Map<String,SpiDescriptor> typeDescriptors;
  
  public Executor executor;
  public Json json;
  public ProcessDefinitionCache processDefinitionCache;
  public ScriptRunnerImpl scriptRunner;
  
  protected ProcessEngineImpl() {
    initialize();
  }

  protected void initialize() {
    initializeId();
    initializeExecutor();
    initializeJson();
    initializeScriptRunner();
    initializePluggableImplementations();
    initializeDefaultSpis();
  }

  protected void initializeId() {
    try {
      id = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      id = "amnesia";
    }
  }
  
  protected void initializePluggableImplementations() {
    activityDescriptors = new HashMap<>();
    typeDescriptors = new HashMap<>();
    dataTypes = new HashMap<>();
    activityTypes = new HashMap<>();
    Iterator<Spi> spis = ServiceLoader.load(Spi.class).iterator();
    while (spis.hasNext()) {
      Spi spiObject = spis.next();
      registerSpi(spiObject);
    }
  }
  
  protected void initializeDefaultSpis() {
    registerSpi(TextType.INSTANCE);
    registerSpi(StartEvent.INSTANCE);
  }

  public ProcessEngineImpl registerJavaBeanType(Class<?> javaBeanClass) {
    registerSpi(new JavaBeanType(javaBeanClass));
    return this;
  }

  public ProcessEngineImpl registerType(Class<? extends DataType> typeClass) {
    registerSpi(Reflection.newInstance(typeClass));
    return this;
  }

  public ProcessEngineImpl registerType(DataType dataType) {
    registerSpi(dataType);
    return this;
  }

  public ProcessEngineImpl registerActivityType(Class<? extends ActivityType> activityTypeClass) {
    registerSpi(Reflection.newInstance(activityTypeClass));
    return this;
  }

  public ProcessEngineImpl registerActivityType(ActivityType activityType) {
    registerSpi(activityType);
    return this;
  }

  void registerSpi(Spi spiObject) {
    if (spiObject==null) {
      return;
    }
    if (spiObject.getId()!=null) {
      if (spiObject instanceof DataType) {
        dataTypes.put(spiObject.getId(), (DataType)spiObject);
      } else if (spiObject instanceof ActivityType) {
        activityTypes.put(spiObject.getId(), (ActivityType)spiObject);
      } else {
        throw new RuntimeException("Unknown Spi type: "+spiObject.getClass().getName());
      }
    } else {
      SpiDescriptor spiDescriptor = new SpiDescriptor(this, spiObject);
      if (spiDescriptor.spiType==SpiType.type) {
        typeDescriptors.put(spiDescriptor.getTypeName(), spiDescriptor);
      } else if (spiObject instanceof ActivityType) {
        activityDescriptors.put(spiDescriptor.getTypeName(), spiDescriptor);
      }
    }
    json.registerSubtype(spiObject.getClass());
  }

  protected void initializeExecutor() {
    this.executor = new ScheduledThreadPoolExecutor(4, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  protected void initializeJson() {
    this.json = new Json(this);
  }
  
  protected void initializeScriptRunner() {
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
    Exceptions.checkNotNull(processBuilder, "processDefinition");

    DeployProcessDefinitionResponse response = new DeployProcessDefinitionResponse();

    ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) processBuilder;
    ProcessValidator validate = new ProcessValidator(this);
    processDefinition.visit(validate);
    ParseIssues issues = validate.getIssues();
    
    if (!issues.hasErrors()) {
      String generatedId = generateProcessDefinitionId(processDefinition);
      processDefinition.id = new ProcessDefinitionId(generatedId);
      response.setProcessDefinitionId(generatedId); 
      storeProcessDefinition(processDefinition);
    } else {
      response.setIssues(issues);
    }
    
    return response;
  }
  
  /** ensures that every element in this process definition has an id */
  protected String generateProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return UUID.randomUUID().toString();
  }
  
  protected abstract void storeProcessDefinition(ProcessDefinitionImpl processDefinition);

  public ProcessInstance startProcessInstance(StartProcessInstanceRequest startProcessInstanceRequest) {
    ProcessDefinitionId processDefinitionId = new ProcessDefinitionId(startProcessInstanceRequest.processDefinitionRefId);
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinitionImpl processDefinition = findProcessDefinitionByIdUsingCache(processDefinitionId);
    ProcessInstanceImpl processInstance = createProcessInstance(processDefinition);
    Map<String, Object> apiValues = startProcessInstanceRequest.variableValues;
    setVariableApiValues(processInstance, apiValues);
      
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
    saveProcessInstance(processInstance);
    processInstance.executeOperations();
    return processInstance;
  }

  private void setVariableApiValues(ScopeInstanceImpl scopeInstance, Map<String, Object> apiValues) {
    if (apiValues!=null) {
      ProcessDefinitionImpl processDefinition = scopeInstance.processDefinition; 
      Map<String, Object> internalValues = new HashMap<>();
      for (String variableDefinitionName: apiValues.keySet()) {
        Object apiValue = apiValues.get(variableDefinitionName);
        VariableDefinitionImpl variableDefinition = processDefinition.findVariableDefinitionByName(variableDefinitionName);
        Object internalValue;
        try {
          internalValue = variableDefinition.dataType.convertJsonToInternalValue(apiValue);
          internalValues.put(variableDefinitionName, internalValue);
        } catch (InvalidApiValueException e) {
          throw new RuntimeException("TODO: this error message should somehow end up in the response", e);
        }
      }
      scopeInstance.setVariableValuesRecursive(apiValues);
    }
  }

  protected ProcessDefinitionImpl findProcessDefinitionByIdUsingCache(ProcessDefinitionId processDefinitionId) {
    ProcessDefinitionImpl processDefinition = processDefinitionCache!=null ? processDefinitionCache.get(processDefinitionId) : null;
    if (processDefinition==null) {
      processDefinition = loadProcessDefinitionById(processDefinitionId);
    }
    return processDefinition;
  }
  
  protected abstract ProcessDefinitionImpl loadProcessDefinitionById(ProcessDefinitionId processDefinitionId);

  protected ProcessInstanceImpl createProcessInstance(ProcessDefinitionImpl processDefinition) {
    return new ProcessInstanceImpl(this, processDefinition, createProcessInstanceId(processDefinition));
  }

  protected ProcessInstanceId createProcessInstanceId(ProcessDefinitionImpl processDefinition) {
    return new ProcessInstanceId(UUID.randomUUID());
  }
  
  /** instantiates and assign an id.
   * activityDefinition is only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the activity instance to the activityDefinition or parent. */
  public ActivityInstanceImpl createActivityInstance(ActivityDefinitionImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    ActivityInstanceId id = createActivityInstanceId();
    activityInstance.setId(id);
    return activityInstance;
  }

  protected ActivityInstanceId createActivityInstanceId() {
    return new ActivityInstanceId(UUID.randomUUID());
  }

  @Override
  public ProcessInstance signal(SignalRequest signalRequest) {
    ActivityInstanceId activityInstanceId = signalRequest.getActivityInstanceId();
    ProcessInstanceImpl processInstance = lockProcessInstanceByActivityInstanceId(activityInstanceId);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(activityInstanceId);
    log.debug("Signalling "+activityInstance);
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.activityType.signal(activityInstance);
    processInstance.executeOperations();
    return processInstance;
  }
  
  public abstract ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId);

  public abstract void saveProcessInstance(ProcessInstanceImpl processInstance);

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

  public ActivityType findActivityType(String activityTypeId) {
    if (activityTypeId==null) {
      return null;
    }
    // first search the user defined activity types
    ActivityType activityType = activityTypes.get(activityTypeId);
    if (activityType!=null) {
      return activityType;
    }
    // then search the default activity types
    activityType = defaultActivityTypes.get(activityTypeId);
    if (activityType!=null) {
      return activityType;
    }
    return null;
  }
  
  public DataType findDataType(String dataTypeId) {
    if (dataTypeId==null) {
      return null;
    }
    // first search the user defined activity types
    DataType dataType = dataTypes.get(dataTypeId);
    if (dataType!=null) {
      return dataType;
    }
    // then search the default activity types
    dataType = defaultTypes.get(dataTypeId);
    if (dataType!=null) {
      return dataType;
    }
    return null;
  }

}

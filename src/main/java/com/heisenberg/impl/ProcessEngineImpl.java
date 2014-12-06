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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ActivityInstanceQuery;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.ParseIssues;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.builder.TriggerBuilder;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.configuration.ProcessEngineConfiguration;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.api.configuration.TaskService;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.util.Page;
import com.heisenberg.api.util.ServiceLocator;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionValidator;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.impl.plugin.ActivityTypes;
import com.heisenberg.impl.plugin.DataTypes;
import com.heisenberg.impl.util.Exceptions;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl extends AbstractProcessEngine implements ProcessEngine, ServiceLocator {

  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public String id;
  public ActivityTypes activityTypes;
  public DataTypes dataTypes;
  public ProcessDefinitionCache processDefinitionCache;
  public ScriptService scriptService;
  public JsonService jsonService;
  public TaskService taskService;
  public Executor executorService;
  
  public ProcessEngineImpl() {
  }
  
  public ProcessEngineImpl(ProcessEngineConfiguration configuration) {
    // construct all objects
    this.id = configuration.getId();
    this.activityTypes = configuration.getActivityTypes();
    this.dataTypes = configuration.getDataTypes();
    this.executorService = configuration.getExecutorService();
    this.processDefinitionCache = configuration.getProcessDefinitionCache();
    this.scriptService = configuration.getScriptService();
    this.jsonService = configuration.getJsonService();
    this.taskService = configuration.getTaskService();
  }

  /// Process Definition Builder 
  
  public DeployResult deployProcessDefinition(ProcessDefinitionBuilder processBuilder) {
    Exceptions.checkNotNull(processBuilder, "processBuilder");
    log.debug("Deploying process");

    DeployResult response = new DeployResult();

    ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) processBuilder;
    ProcessDefinitionValidator validator = new ProcessDefinitionValidator(this);
    processDefinition.visit(validator);
    ParseIssues issues = validator.getIssues();
    response.setIssues(issues);
    
    if (!issues.hasErrors()) {
      processDefinition.id = createProcessDefinitionId(processDefinition);
      response.setProcessDefinitionId(processDefinition.id); 
      insertProcessDefinition(processDefinition);
      processDefinitionCache.put(processDefinition);
    }
    
    return response;
  }
  
  public ProcessInstance startProcessInstance(TriggerBuilderImpl processInstanceBuilder) {
    String processDefinitionId = processInstanceBuilder.processDefinitionId;
    Exceptions.checkNotNullParameter(processDefinitionId, "processDefinitionId");
    ProcessDefinitionImpl processDefinition = findProcessDefinitionByIdUsingCache(processDefinitionId);
    if (processDefinition==null) {
      throw new RuntimeException("Could not find process definition "+processDefinitionId);
    }
    ProcessInstanceImpl processInstance = createProcessInstance(processDefinition);
    processInstance.transientContext = processInstanceBuilder.transientContext;
    setVariableApiValues(processInstance, processInstanceBuilder);
      
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
  
  @Override
  public MessageImpl newMessage() {
    return new MessageImpl(this);
  }
  
  public ProcessInstanceImpl sendActivityInstanceMessage(MessageImpl notifyActivityInstanceBuilder) {
    String activityInstanceId = notifyActivityInstanceBuilder.activityInstanceId;
    String processInstanceId = notifyActivityInstanceBuilder.processInstanceId;
    ProcessInstanceImpl processInstance = lockProcessInstanceByActivityInstanceId(processInstanceId, activityInstanceId);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(activityInstanceId);
    if (activityInstance.isEnded()) {
      throw new RuntimeException("Activity instance "+activityInstance+" is already ended");
    }
    log.debug("Signalling "+activityInstance);
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.activityType.message(activityInstance);
    processInstance.executeOperations();
    return processInstance;
  }
  
  /** ensures that every element in this process definition has an id */
  protected String createProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return UUID.randomUUID().toString();
  }
  
  /** @param processDefinition is a validated process definition that has no errors.  It might have warnings. */
  protected abstract void insertProcessDefinition(ProcessDefinitionImpl processDefinition);

  private void setVariableApiValues(ScopeInstanceImpl scopeInstance, VariableRequestImpl variableRequest) {
    ProcessDefinitionImpl processDefinition = scopeInstance.processDefinition;
    Map<String, Object> variableValues = variableRequest.variableValues;
    if (variableValues!=null) {
      for (Object variableDefinitionId: variableValues.keySet()) {
        Object internalValue = variableValues.get(variableDefinitionId);
        VariableDefinitionImpl variableDefinition = processDefinition.findVariableDefinition(variableDefinitionId);
        variableDefinition.dataType.validateInternalValue(internalValue);
      }
      if (variableValues!=null) {
        scopeInstance.setVariableValuesRecursive(variableValues);
      }
    }
  }

  public ProcessDefinitionImpl findProcessDefinitionByIdUsingCache(String processDefinitionId) {
    ProcessDefinitionImpl processDefinition = processDefinitionCache.get(processDefinitionId);
    if (processDefinition==null) {
      processDefinition = loadProcessDefinitionById(processDefinitionId);
      processDefinitionCache.put(processDefinition);
    }
    return processDefinition;
  }
  
  protected abstract ProcessDefinitionImpl loadProcessDefinitionById(String processDefinitionId);

  protected ProcessInstanceImpl createProcessInstance(ProcessDefinitionImpl processDefinition) {
    return new ProcessInstanceImpl(this, processDefinition, createProcessInstanceId(processDefinition));
  }

  protected String createProcessInstanceId(ProcessDefinitionImpl processDefinition) {
    return UUID.randomUUID().toString();
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

  protected String createActivityInstanceId() {
    return UUID.randomUUID().toString();
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

  protected String createVariableInstanceId() {
    return UUID.randomUUID().toString();
  }

  public abstract ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(String processInstanceId, String activityInstanceId);

  public abstract void insertProcessInstance(ProcessInstanceImpl processInstance);

  public abstract void flush(ProcessInstanceImpl processInstance);

  public abstract void flushAndUnlock(ProcessInstanceImpl processInstance);
  
  @Override
  public ActivityInstanceQuery newActivityInstanceQuery() {
    return new ActivityInstanceQueryImpl(this);
  }

  public String getId() {
    return id;
  }
  
  public Executor getExecutorService() {
    return executorService;
  }
  
  public JsonService getJsonService() {
    return jsonService;
  }
  
  public ScriptService getScriptService() {
    return scriptService;
  }
  
  public TaskService getTaskService() {
    return taskService;
  }
  
  public ActivityTypes getActivityTypes() {
    return activityTypes;
  }
  
  public DataTypes getDataTypes() {
    return dataTypes;
  }

  public abstract List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQuery processInstanceQuery);

  public abstract List<ProcessDefinitionImpl> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery);

  public abstract Page<ActivityInstance> findActivityInstances(ActivityInstanceQueryImpl activityInstanceQueryImpl);
}

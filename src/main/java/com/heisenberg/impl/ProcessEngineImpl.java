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

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.ParseIssues;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.configuration.ProcessEngineConfiguration;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.api.configuration.TaskService;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.plugin.ProcessProfileBuilder;
import com.heisenberg.api.util.ServiceLocator;
import com.heisenberg.impl.ProcessDefinitionQueryImpl.Representation;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionValidator;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.impl.job.JobService;
import com.heisenberg.impl.util.Exceptions;
import com.heisenberg.impl.util.Lists;
import com.mongodb.BasicDBObject;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl extends AbstractProcessEngine implements ProcessEngine, ServiceLocator {

  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public String id;
  public ProcessDefinitionCache processDefinitionCache;
  public ScriptService scriptService;
  public TaskService taskService;
  public ExecutorService executorService;
  public JobService jobService;
  public String builderUrl;
  
  public ProcessEngineImpl() {
  }
  
  public ProcessEngineImpl(ProcessEngineConfiguration configuration) {
    super(configuration);
    this.id = configuration.getId();
    this.executorService = configuration.getExecutorService();
    this.processDefinitionCache = configuration.getProcessDefinitionCache();
    this.scriptService = configuration.getScriptService();
    this.taskService = configuration.getTaskService();
  }
  
  public void startup() {
  }

  public void shutdown() {
    executorService.shutdown();
  }

  /// Process Definition Builder 
  
  public DeployResult deployProcessDefinition(ProcessDefinitionBuilder processBuilder) {
    Exceptions.checkNotNull(processBuilder, "processBuilder");
    log.debug("Deploying process");

    DeployResult response = new DeployResult();

    ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) processBuilder;
    processDefinition.deployedTime = new LocalDateTime();
    
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
  
  @Override
  public ProcessProfileBuilder newProcessProfile() {
    return new ProcessProfileImpl(this);
  }
  
  public String updateProcessProfile(ProcessProfileImpl profile) {
    log.debug("Sending process profile over HTTP to the process builder:");
    log.debug(">>> PUT "+profile.getUrl());
    // TODO send over HTTP to the server
    ProcessProfileRequestBody processProfileBody = new ProcessProfileRequestBody();
    processProfileBody.key = profile.profileKey;
    processProfileBody.name = profile.profileName;
    processProfileBody.activityDescriptors = activityTypeService.descriptors;
    processProfileBody.dataTypeDescriptors = dataTypeService.descriptors;
    processProfileBody.dataSourceDescriptors = dataSourceService.descriptors;
    processProfileBody.triggerDescriptors = triggerService.descriptors;
    log.debug(">>> "+jsonService.objectToJsonStringPretty(processProfileBody));
    return null;
  }

  public ProcessInstance startProcessInstance(StartBuilderImpl start) {
    ProcessDefinitionImpl processDefinition = newProcessDefinitionQuery()
      .representation(Representation.EXECUTABLE)
      .id(start.processDefinitionId)
      .name(start.processDefinitionName)
      .orderByDeployTimeDescending()
      .get();
    
    if (processDefinition==null) {
      throw new RuntimeException("Could not find process definition "+start.processDefinitionId+" "+start.processDefinitionName);
    }
    ProcessInstanceImpl processInstance = createProcessInstance(processDefinition);
    processInstance.callerProcessInstanceId = start.callerProcessInstanceId;
    processInstance.callerActivityInstanceId = start.callerActivityInstanceId;
    processInstance.transientContext = start.transientContext;
    setVariableApiValues(processInstance, start);
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
  
  public ProcessInstanceImpl sendActivityInstanceMessage(MessageImpl message) {
    ProcessInstanceQueryImpl query = newProcessInstanceQuery()
      .id(message.processInstanceId)
      .activityInstanceId(message.activityInstanceId);
    ProcessInstanceImpl processInstance = lockProcessInstanceWithRetry(query);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(message.activityInstanceId);
    if (activityInstance.isEnded()) {
      throw new RuntimeException("Activity instance "+activityInstance+" is already ended");
    }
    log.debug("Signalling "+activityInstance);
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.activityType.message(activityInstance);
    processInstance.executeOperations();
    return processInstance;
  }
  
  public ProcessInstanceImpl lockProcessInstanceWithRetry(ProcessInstanceQueryImpl query) {
    long wait = 50l;
    long attempts = 0;
    long maxAttempts = 4;
    long backofFactor = 5;
    ProcessInstanceImpl processInstance = lockProcessInstance(query);
    while ( processInstance==null 
            && attempts <= maxAttempts ) {
      try {
        log.debug("Locking failed... retrying");
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        log.debug("Waiting for lock to be released was interrupted");
      }
      wait = wait * backofFactor;
      attempts++;
      processInstance = lockProcessInstance(query);
    }
    if (processInstance==null) {
      throw new RuntimeException("Couldn't lock process instance with "+query);
    }
    return processInstance;
  }

  
  /** ensures that every element in this process definition has an id */
  protected String createProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return UUID.randomUUID().toString();
  }
  
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
        scopeInstance.setVariableValues(variableValues);
      }
    }
  }
  
  public ProcessDefinitionQueryImpl newProcessDefinitionQuery() {
    return new ProcessDefinitionQueryImpl(this);
  }
  
  public List<ProcessDefinitionImpl> findProcessDefinitions(ProcessDefinitionQueryImpl query) {
    if (query.onlyIdSpecified()) {
      ProcessDefinitionImpl cachedProcessDefinition = processDefinitionCache.get(query.id);
      if (cachedProcessDefinition!=null) {
        return Lists.of(cachedProcessDefinition);
      }
    }
    List<ProcessDefinitionImpl> result = loadProcessDefinitions(query);
    if (Representation.EXECUTABLE==query.representation) {
      for (ProcessDefinitionImpl processDefinition: result) {
        ProcessDefinitionValidator validator = new ProcessDefinitionValidator(this);
        processDefinition.visit(validator);
        processDefinitionCache.put(processDefinition);
      }
    }
    return result;
  }

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

  @Override
  public ProcessInstanceQueryImpl newProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(this);
  }

  public String getId() {
    return id;
  }
  
  public ExecutorService getExecutorService() {
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
  
  /** @param processDefinition is a validated process definition that has no errors.  It might have warnings. */
  public abstract void insertProcessDefinition(ProcessDefinitionImpl processDefinition);

  public abstract List<ProcessDefinitionImpl> loadProcessDefinitions(ProcessDefinitionQueryImpl processDefinitionQuery);

  public abstract List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQueryImpl processInstanceQuery);

  public abstract ProcessInstanceImpl lockProcessInstance(ProcessInstanceQueryImpl processInstance);

  public abstract void insertProcessInstance(ProcessInstanceImpl processInstance);

  public abstract ProcessInstanceImpl findProcessInstanceById(String processInstanceId);

  public abstract void flush(ProcessInstanceImpl processInstance);

  public abstract void flushAndUnlock(ProcessInstanceImpl processInstance);

}

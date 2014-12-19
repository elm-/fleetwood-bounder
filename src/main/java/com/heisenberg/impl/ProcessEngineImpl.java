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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.DataTypes;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.ProcessEngineConfiguration;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.ParseIssues;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ProcessInstance;
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
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.util.Exceptions;
import com.heisenberg.impl.util.Lists;
import com.heisenberg.memory.MemoryJobServiceImpl;
import com.heisenberg.memory.MemoryTaskService;
import com.heisenberg.memory.MemoryWorkflowInstanceStore;
import com.heisenberg.memory.MemoryWorkflowStore;
import com.heisenberg.plugin.ServiceRegistry;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl implements ProcessEngine {

  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public String id;
  public ServiceRegistry serviceRegistry;
  
  public DataTypes dataTypes;
  public JsonService jsonService;
  public ExecutorService executorService;
  public ProcessDefinitionCache processDefinitionCache;
  public WorkflowStore workflowStore;
  public WorkflowInstanceStore workflowInstanceStore;

  protected ProcessEngineImpl() {
  }

  protected ProcessEngineImpl(ProcessEngineConfiguration configuration) {
    this.serviceRegistry = configuration.getServiceRegistry();
    this.serviceRegistry.registerService(this);
    initializeId(configuration);
    initializeStorageServices(configuration);
    this.dataTypes = serviceRegistry.getService(DataTypes.class);
    this.jsonService = serviceRegistry.getService(JsonService.class);
    this.executorService = serviceRegistry.getService(ExecutorService.class);
    this.processDefinitionCache = serviceRegistry.getService(ProcessDefinitionCache.class);
    this.workflowStore = serviceRegistry.getService(WorkflowStore.class);
    this.workflowInstanceStore = serviceRegistry.getService(WorkflowInstanceStore.class);
  }
  
  protected void initializeStorageServices(ProcessEngineConfiguration configuration) {
    configuration.registerService(new MemoryWorkflowStore(serviceRegistry));
    configuration.registerService(new MemoryWorkflowInstanceStore(serviceRegistry));
    configuration.registerService(new MemoryTaskService(serviceRegistry));
    configuration.registerService(new MemoryJobServiceImpl(serviceRegistry));
  }

  protected void initializeId(ProcessEngineConfiguration configuration) {
    this.id = configuration.getId();
    if (id==null) {
      try {
        id = InetAddress.getLocalHost().getHostAddress();
        try {
          String processName = ManagementFactory.getRuntimeMXBean().getName();
          int atIndex = processName.indexOf('@');
          if (atIndex > 0) {
            id += ":" + processName.substring(0, atIndex);
          }
        } catch (Exception e) {
          id += ":?";
        }
      } catch (UnknownHostException e1) {
        id = UUID.randomUUID().toString();
      }
    }
  }

  public void startup() {
  }

  public void shutdown() {
    executorService.shutdown();
  }

  @Override
  public DataTypes getDataTypes() {
    return dataTypes;
  }

  @Override
  public ProcessDefinitionBuilder newProcessDefinition() {
    return new ProcessDefinitionImpl(this);
  }

  @Override
  public StartBuilderImpl newStart() {
    return new StartBuilderImpl(this, jsonService);
  }

  @Override
  public MessageBuilder newMessage() {
    return new MessageImpl(this, jsonService);
  }

  @Override
  public ProcessInstanceQueryImpl newProcessInstanceQuery() {
    return new ProcessInstanceQueryImpl(workflowInstanceStore);
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
      processDefinition.id = workflowStore.createProcessDefinitionId(processDefinition);
      response.setProcessDefinitionId(processDefinition.id); 
      workflowStore.insertProcessDefinition(processDefinition);
      processDefinitionCache.put(processDefinition);
    }
    
    return response;
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
    workflowInstanceStore.insertProcessInstance(processInstance);
    processInstance.executeOperations();
    return processInstance;
  }
  
  public ProcessInstanceImpl sendActivityInstanceMessage(MessageImpl message) {
    ProcessInstanceQueryImpl query = newProcessInstanceQuery()
      .processInstanceId(message.processInstanceId)
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
    ProcessInstanceImpl processInstance = workflowInstanceStore.lockProcessInstance(query);
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
      processInstance = workflowInstanceStore.lockProcessInstance(query);
    }
    if (processInstance==null) {
      throw new RuntimeException("Couldn't lock process instance with "+query);
    }
    return processInstance;
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
    List<ProcessDefinitionImpl> result = workflowStore.loadProcessDefinitions(query);
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
    String processInstanceId = workflowInstanceStore.createProcessInstanceId(processDefinition);
    return new ProcessInstanceImpl(this, processDefinition, processInstanceId);
  }

  /** instantiates and assign an id.
   * parent and activityDefinition are only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the parent or the activityDefinition. */
  public ActivityInstanceImpl createActivityInstance(ScopeInstanceImpl parent, ActivityDefinitionImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = workflowInstanceStore.createActivityInstanceId();
    return activityInstance;
  }

  /** instantiates and assign an id.
   * parent and variableDefinition are only passed for reference.  
   * Apart from choosing the variable instance class to instantiate and assigning the id,
   * this method does not need to link the parent or variableDefinition. */
  public VariableInstanceImpl createVariableInstance(ScopeInstanceImpl parent, VariableDefinitionImpl variableDefinition) {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.id = workflowInstanceStore.createVariableInstanceId();
    return variableInstance;
  }

  public String getId() {
    return id;
  }

  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }
  
  public JsonService getJsonService() {
    return jsonService;
  }
  
  public ExecutorService getExecutorService() {
    return executorService;
  }

  public ProcessDefinitionCache getProcessDefinitionCache() {
    return processDefinitionCache;
  }

  public WorkflowStore getWorkflowStore() {
    return workflowStore;
  }
  
  public WorkflowInstanceStore getWorkflowInstanceStore() {
    return workflowInstanceStore;
  }
}

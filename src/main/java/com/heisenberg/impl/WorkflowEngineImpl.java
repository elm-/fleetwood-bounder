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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.heisenberg.api.instance.WorkflowInstanceEventListener;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.DataTypes;
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.WorkflowEngineConfiguration;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.ParseIssues;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.WorkflowQueryImpl.Representation;
import com.heisenberg.impl.definition.ActivityImpl;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.definition.WorkflowValidator;
import com.heisenberg.impl.definition.VariableImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.memory.MemoryJobServiceImpl;
import com.heisenberg.impl.memory.MemoryTaskService;
import com.heisenberg.impl.memory.MemoryWorkflowInstanceStore;
import com.heisenberg.impl.memory.MemoryWorkflowStore;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.impl.util.Exceptions;
import com.heisenberg.impl.util.Lists;

/**
 * @author Walter White
 */
public abstract class WorkflowEngineImpl implements WorkflowEngine {

  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public String id;
  public ServiceRegistry serviceRegistry;
  
  public DataTypes dataTypes;
  public JsonService jsonService;
  public ExecutorService executorService;
  public WorkflowCache workflowCache;
  public WorkflowStore workflowStore;
  public WorkflowInstanceStore workflowInstanceStore;

  private List<WorkflowInstanceEventListener> listeners;

  protected WorkflowEngineImpl(WorkflowEngineConfiguration configuration) {
    this.serviceRegistry = configuration.getServiceRegistry();
    this.serviceRegistry.registerService(this);
    initializeId(configuration);
    initializeStorageServices(configuration);
    this.dataTypes = serviceRegistry.getService(DataTypes.class);
    this.jsonService = serviceRegistry.getService(JsonService.class);
    this.executorService = serviceRegistry.getService(ExecutorService.class);
    this.workflowCache = serviceRegistry.getService(WorkflowCache.class);
    this.workflowStore = serviceRegistry.getService(WorkflowStore.class);
    this.workflowInstanceStore = serviceRegistry.getService(WorkflowInstanceStore.class);
    this.listeners = new ArrayList<>();
  }
  
  protected void initializeStorageServices(WorkflowEngineConfiguration configuration) {
    configuration.registerService(new MemoryWorkflowStore(serviceRegistry));
    configuration.registerService(new MemoryWorkflowInstanceStore(serviceRegistry));
    configuration.registerService(new MemoryTaskService(serviceRegistry));
    configuration.registerService(new MemoryJobServiceImpl(serviceRegistry));
  }

  protected void initializeId(WorkflowEngineConfiguration configuration) {
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

  /// Workflow methods //////////////////////////////////////////////////////////// 
  
  @Override
  public WorkflowBuilder newWorkflow() {
    return new WorkflowImpl(this);
  }

  public DeployResult deployWorkflow(WorkflowBuilder processBuilder) {
    Exceptions.checkNotNull(processBuilder, "processBuilder");
    log.debug("Deploying process");

    DeployResult response = new DeployResult();

    WorkflowImpl processDefinition = (WorkflowImpl) processBuilder;
    processDefinition.deployedTime = new LocalDateTime();
    
    WorkflowValidator validator = new WorkflowValidator(this);
    processDefinition.visit(validator);
    ParseIssues issues = validator.getIssues();
    response.setIssues(issues);

    if (!issues.hasErrors()) {
      processDefinition.id = workflowStore.createWorkflowId(processDefinition);
      response.setProcessDefinitionId(processDefinition.id); 
      workflowStore.insertWorkflow(processDefinition);
      workflowCache.put(processDefinition);
    }
    
    return response;
  }

  public WorkflowQueryImpl newWorkflowQuery() {
    return new WorkflowQueryImpl(this);
  }
  
  public List<WorkflowImpl> findWorkflows(WorkflowQueryImpl query) {
    if (query.onlyIdSpecified()) {
      WorkflowImpl cachedProcessDefinition = workflowCache.get(query.id);
      if (cachedProcessDefinition!=null) {
        return Lists.of(cachedProcessDefinition);
      }
    }
    List<WorkflowImpl> result = workflowStore.loadWorkflows(query);
    if (Representation.EXECUTABLE==query.representation) {
      for (WorkflowImpl processDefinition: result) {
        WorkflowValidator validator = new WorkflowValidator(this);
        processDefinition.visit(validator);
        workflowCache.put(processDefinition);
      }
    }
    return result;
  }

  @Override
  public void deleteWorkflow(String workflowId) {
    workflowStore.deleteWorkflow(workflowId);
  }

  /// Workflow instance methods //////////////////////////////////////////////////////////// 
  
  @Override
  public StartImpl newStart() {
    return new StartImpl(this, jsonService);
  }

  @Override
  public MessageBuilder newMessage() {
    return new MessageImpl(this, jsonService);
  }

  @Override
  public WorkflowInstanceQueryImpl newWorkflowInstanceQuery() {
    return new WorkflowInstanceQueryImpl(workflowInstanceStore);
  }

  public WorkflowInstance startProcessInstance(StartImpl start) {
    WorkflowImpl processDefinition = newWorkflowQuery()
      .representation(Representation.EXECUTABLE)
      .id(start.processDefinitionId)
      .name(start.processDefinitionName)
      .orderByDeployTimeDescending()
      .get();
    
    if (processDefinition==null) {
      throw new RuntimeException("Could not find process definition "+start.processDefinitionId+" "+start.processDefinitionName);
    }
    WorkflowInstanceImpl processInstance = createProcessInstance(processDefinition);
    processInstance.callerWorkflowInstanceId = start.callerWorkflowInstanceId;
    processInstance.callerActivityInstanceId = start.callerActivityInstanceId;
    processInstance.transientContext = start.transientContext;
    setVariableApiValues(processInstance, start);
    log.debug("Starting "+processInstance);
    processInstance.setStart(Time.now());
    List<Activity> startActivityDefinitions = processDefinition.getStartActivities();
    if (startActivityDefinitions!=null) {
      for (Activity startActivityDefinition: startActivityDefinitions) {
        processInstance.start(startActivityDefinition);
      }
    }
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    processInstance.setLock(lock);
    workflowInstanceStore.insertWorkflowInstance(processInstance);
    processInstance.executeWork();
    return processInstance;
  }
  
  public WorkflowInstanceImpl sendActivityInstanceMessage(MessageImpl message) {
    WorkflowInstanceQueryImpl query = newWorkflowInstanceQuery()
      .workflowInstanceId(message.processInstanceId)
      .activityInstanceId(message.activityInstanceId);
    WorkflowInstanceImpl processInstance = lockProcessInstanceWithRetry(query);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(message.activityInstanceId);
    if (activityInstance.isEnded()) {
      throw new RuntimeException("Activity instance "+activityInstance+" is already ended");
    }
    log.debug("Signalling "+activityInstance);
    ActivityImpl activityDefinition = activityInstance.getActivity();
    activityDefinition.activityType.message(activityInstance);
    processInstance.executeWork();
    return processInstance;
  }
  
  public WorkflowInstanceImpl lockProcessInstanceWithRetry(WorkflowInstanceQueryImpl query) {
    long wait = 50l;
    long attempts = 0;
    long maxAttempts = 4;
    long backoffFactor = 5;
    WorkflowInstanceImpl processInstance = workflowInstanceStore.lockWorkflowInstance(query);
    while ( processInstance==null 
            && attempts <= maxAttempts ) {
      try {
        log.debug("Locking failed... retrying");
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        log.debug("Waiting for lock to be released was interrupted");
      }
      wait = wait * backoffFactor;
      attempts++;
      processInstance = workflowInstanceStore.lockWorkflowInstance(query);
    }
    if (processInstance==null) {
      throw new RuntimeException("Couldn't lock process instance with "+query);
    }
    return processInstance;
  }

  
  private void setVariableApiValues(ScopeInstanceImpl scopeInstance, VariableRequestImpl variableRequest) {
    WorkflowImpl processDefinition = scopeInstance.workflow;
    Map<String, Object> variableValues = variableRequest.variableValues;
    if (variableValues!=null) {
      for (Object variableDefinitionId: variableValues.keySet()) {
        Object internalValue = variableValues.get(variableDefinitionId);
        VariableImpl variableDefinition = processDefinition.findVariable(variableDefinitionId);
        variableDefinition.dataType.validateInternalValue(internalValue);
      }
      if (variableValues!=null) {
        scopeInstance.setVariableValues(variableValues);
      }
    }
  }
  
  @Override
  public void deleteWorkflowInstance(String workflowInstanceId) {
    workflowInstanceStore.deleteWorkflowInstance(workflowInstanceId);
    
  }


  protected WorkflowInstanceImpl createProcessInstance(WorkflowImpl processDefinition) {
    String processInstanceId = workflowInstanceStore.createWorkflowInstanceId(processDefinition);
    return new WorkflowInstanceImpl(this, processDefinition, processInstanceId);
  }

  /** instantiates and assign an id.
   * parent and activityDefinition are only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the parent or the activityDefinition. */
  public ActivityInstanceImpl createActivityInstance(ScopeInstanceImpl parent, ActivityImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = workflowInstanceStore.createActivityInstanceId();
    return activityInstance;
  }

  /** instantiates and assign an id.
   * parent and variableDefinition are only passed for reference.  
   * Apart from choosing the variable instance class to instantiate and assigning the id,
   * this method does not need to link the parent or variableDefinition. */
  public VariableInstanceImpl createVariableInstance(ScopeInstanceImpl parent, VariableImpl variableDefinition) {
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

  public WorkflowCache getProcessDefinitionCache() {
    return workflowCache;
  }

  public WorkflowStore getWorkflowStore() {
    return workflowStore;
  }
  
  public WorkflowInstanceStore getWorkflowInstanceStore() {
    return workflowInstanceStore;
  }

  public void addListener(WorkflowInstanceEventListener listener) {
    synchronized (listener) {
      listeners.add(listener);
    }
  }

  public void removeListener(WorkflowInstanceEventListener listener) {
    synchronized (listener) {
      listeners.remove(listener);
    }
  }

  public List<WorkflowInstanceEventListener> getListeners() {
    return Collections.unmodifiableList(listeners);
  }
}

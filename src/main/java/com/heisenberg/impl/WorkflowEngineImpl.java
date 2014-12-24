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

import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_NOTIFYING;
import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING;
import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_CONTAINER;
import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_INSTANCE;

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
import com.heisenberg.api.activitytypes.CallActivity;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.ParseIssues;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.WorkflowQueryImpl.Representation;
import com.heisenberg.impl.definition.ActivityImpl;
import com.heisenberg.impl.definition.TransitionImpl;
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

  protected WorkflowEngineImpl() {
  }

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

  public ParseIssues validateWorkflow(WorkflowImpl workflow) {
    // throws an exception if there are errors 
    WorkflowValidator validator = new WorkflowValidator(this);
    workflow.visit(validator);
    return validator.getIssues();
  }

  public DeployResult validateAndDeploy(WorkflowImpl workflow) {
    Exceptions.checkNotNull(workflow, "processBuilder");
    log.debug("Deploying process");
    workflow.deployedTime = new LocalDateTime();

    DeployResult deployResult = new DeployResult();

    // throws an exception if there are errors 
    WorkflowValidator validator = new WorkflowValidator(this);
    workflow.visit(validator);
    ParseIssues issues = validator.getIssues();
    deployResult.setIssues(issues);
    
    if (!issues.hasErrors()) {
      workflow.id = workflowStore.createWorkflowId(workflow);
      deployResult.setWorkflowId(workflow.id);

      workflowStore.insertWorkflow(workflow);
      workflowCache.put(workflow);
    }
    
    return deployResult;
  }

  public String deployWorkflow(WorkflowImpl workflow) {
    return validateAndDeploy(workflow)
            .getWorkflowId();
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
    processInstance.workflowEngine.executeWork(processInstance);
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
    processInstance.workflowEngine.executeWork(processInstance);
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
  
  // process execution methods ////////////////////////////////////////////////////////
  
  
  

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

  public void executeWork(final WorkflowInstanceImpl workflowInstance) {
    WorkflowInstanceStore workflowInstanceStore = getWorkflowInstanceStore();
    boolean isFirst = true;
    while (workflowInstance.hasWork()) {
      // in the first iteration, the updates will be empty and hence no updates will be flushed
      if (isFirst) {
        isFirst = false;
      } else {
        workflowInstanceStore.flush(workflowInstance); 
      }
      ActivityInstanceImpl activityInstance = workflowInstance.getNextWork();
      ActivityImpl activity = activityInstance.getActivity();
      
      if (STATE_STARTING.equals(activityInstance.workState)) {
        log.debug("Starting "+activityInstance);
        executeStart(activityInstance);
        
      } else if (STATE_STARTING_MULTI_INSTANCE.equals(activityInstance.workState)) {
        log.debug("Starting multi instance "+activityInstance);
        executeStart(activityInstance);
        
      } else if (STATE_STARTING_MULTI_CONTAINER.equals(activityInstance.workState)) {
        List<Object> values = activityInstance.getValue(activity.multiInstance);
        if (values!=null && !values.isEmpty()) {
          log.debug("Starting multi container "+activityInstance);
          for (Object value: values) {
            ActivityInstanceImpl elementActivityInstance = activityInstance.createActivityInstance(activity);
            elementActivityInstance.setWorkState(STATE_STARTING_MULTI_INSTANCE); 
            elementActivityInstance.initializeForEachElement(activity.multiInstanceElement, value);
          }
        } else {
          log.debug("Skipping empty multi container "+activityInstance);
          activityInstance.onwards();
        }
  
      } else if (STATE_NOTIFYING.equals(activityInstance.workState)) {
        log.debug("Notifying parent of "+activityInstance);
        activityInstance.parent.ended(activityInstance);
        activityInstance.workState = null;
      }
    }
    if (workflowInstance.hasAsyncWork()) {
      log.debug("Going asynchronous "+workflowInstance.workflowInstance);
      workflowInstanceStore.flush(workflowInstance.workflowInstance);
      ExecutorService executor = getExecutorService();
      executor.execute(new Runnable(){
        public void run() {
          try {
            workflowInstance.work = workflowInstance.workAsync;
            workflowInstance.workAsync = null;
            workflowInstance.workflowInstance.isAsync = true;
            if (workflowInstance.updates!=null) {
              workflowInstance.getUpdates().isWorkChanged = true;
              workflowInstance.getUpdates().isAsyncWorkChanged = true;
            }
            executeWork(workflowInstance);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }});
    } else {
      workflowInstanceStore.flushAndUnlock(workflowInstance.workflowInstance);
    }
  }
  
  public void executeStart(ActivityInstanceImpl activityInstance) {
    for (WorkflowInstanceEventListener listener : listeners) {
      listener.started(activityInstance);
    }
    ActivityImpl activity = activityInstance.getActivity();
    activity.activityType.start(activityInstance);
    if (ActivityInstanceImpl.START_WORKSTATES.contains(activityInstance.workState)) {
      activityInstance.setWorkState(ActivityInstanceImpl.STATE_WAITING);
    }
  }

  public void executeWorkflowInstanceEnded(WorkflowInstanceImpl workflowInstance) {
    if (workflowInstance.callerWorkflowInstanceId!=null) {
      WorkflowInstanceQueryImpl processInstanceQuery = newWorkflowInstanceQuery()
       .workflowInstanceId(workflowInstance.callerWorkflowInstanceId)
       .activityInstanceId(workflowInstance.callerActivityInstanceId);
      WorkflowInstanceImpl callerProcessInstance = lockProcessInstanceWithRetry(processInstanceQuery);
      ActivityInstanceImpl callerActivityInstance = callerProcessInstance.findActivityInstance(workflowInstance.callerActivityInstanceId);
      if (callerActivityInstance.isEnded()) {
        throw new RuntimeException("Call activity instance "+callerActivityInstance+" is already ended");
      }
      log.debug("Notifying caller "+callerActivityInstance);
      ActivityImpl activityDefinition = callerActivityInstance.getActivity();
      CallActivity callActivity = (CallActivity) activityDefinition.activityType;
      callActivity.calledProcessInstanceEnded(callerActivityInstance, workflowInstance);
      callerActivityInstance.onwards();
      executeWork(callerProcessInstance);
    }
  }

  public void executeOnwards(ActivityInstanceImpl activityInstance) {
    log.debug("Onwards "+this);
    ActivityImpl activity = activityInstance.activityDefinition;
    // Default BPMN logic when an activity ends
    // If there are outgoing transitions (in bpmn they are called sequence flows)
    if (activity.hasOutgoingTransitionDefinitions()) {
      // Ensure that each transition is taken
      // Note that process concurrency does not require java concurrency
      activityInstance.end(false);
      for (TransitionImpl transitionDefinition: activity.outgoingDefinitions) {
        activityInstance.takeTransition(transitionDefinition);
      }
    } else {
      // Propagate completion upwards
      activityInstance.end(true);
    }
  }
  
  public void executeEnd(ActivityInstanceImpl activityInstance, boolean notifyParent) {
    if (activityInstance.end==null) {
      if (activityInstance.hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are open activity instances: " +activityInstance);
      }
      activityInstance.setEnd(Time.now());
      for (WorkflowInstanceEventListener listener : listeners) {
        listener.ended(activityInstance);
      }
      if (notifyParent) {
        activityInstance.setWorkState(STATE_NOTIFYING);
        activityInstance.workflowInstance.addWork(activityInstance);

      } else {
        activityInstance.setWorkState(null); // means please archive me.
      }
    }
  }
}

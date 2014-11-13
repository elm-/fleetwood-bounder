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
package com.heisenberg.instance;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.CreateProcessInstanceRequest;
import com.heisenberg.ProcessDefinitionQueryBuilder;
import com.heisenberg.ProcessEngine;
import com.heisenberg.ProcessInstanceQueryBuilder;
import com.heisenberg.SignalRequest;
import com.heisenberg.definition.ActivityDefinition;
import com.heisenberg.definition.EnsureIdVisitor;
import com.heisenberg.definition.ProcessDefinition;
import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.definition.VariableDefinitionId;
import com.heisenberg.util.Exceptions;
import com.heisenberg.util.Time;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl implements ProcessEngine {

  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  protected String id;
  protected ProcessEngine processEngine;
  protected Executor executor;
  
  public ProcessDefinition saveProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    identifyProcessDefinition(processDefinition);
    storeProcessDefinition(processDefinition);
    return processDefinition;
  }
  
  /** ensures that every element in this process definition has an id */
  protected void identifyProcessDefinition(ProcessDefinition processDefinition) {
    processDefinition.visit(new EnsureIdVisitor(this));
  }
  
  protected abstract void storeProcessDefinition(ProcessDefinition processDefinition);

  public ProcessInstance createProcessInstance(CreateProcessInstanceRequest createProcessInstanceRequest) {
    ProcessDefinitionId processDefinitionId = createProcessInstanceRequest.getProcessDefinitionId();
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinition processDefinition = buildProcessDefinitionQuery()
      .processDefinitionId(processDefinitionId)
      .get();
    ProcessInstanceId processInstanceId = createProcessInstanceRequest.getProcessInstanceId();
    ProcessInstance processInstance = createProcessInstance(processDefinition, processInstanceId);
    Map<VariableDefinitionId, Object> variableValues = createProcessInstanceRequest.getVariableValues();
    processInstance.setVariableValuesRecursive(variableValues);
    log.debug("Starting "+processInstance);
    processInstance.setStart(Time.now());
    List<ActivityDefinition> startActivityDefinitions = processDefinition.getStartActivityDefinitions();
    if (startActivityDefinitions!=null) {
      for (ActivityDefinition startActivityDefinition: startActivityDefinitions) {
        ActivityInstance activityInstance = processInstance.createActivityInstance(startActivityDefinition);
        processInstance.startActivityInstance(activityInstance);
      }
    }
    Lock lock = new Lock();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    processInstance.setLock(lock);
    saveProcessInstance(processInstance);
    processInstance.executeOperations();
    return processInstance;
  }

  protected ProcessInstance createProcessInstance(ProcessDefinition processDefinition, ProcessInstanceId processInstanceId) {
    if (processInstanceId==null) {
      processInstanceId = createProcessInstanceId(processDefinition);
    }
    ProcessInstance processInstance = new ProcessInstance(this, processDefinition, processInstanceId);
    return processInstance;
  }

  protected ProcessInstanceId createProcessInstanceId(ProcessDefinition processDefinition) {
    return new ProcessInstanceId(UUID.randomUUID());
  }
  
  /** instantiates and assign an id.
   * activityDefinition is only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the activity instance to the activityDefinition or parent. */
  public ActivityInstance createActivityInstance(ActivityDefinition activityDefinition) {
    ActivityInstance activityInstance = new ActivityInstance();
    ActivityInstanceId id = createActivityInstanceId();
    activityInstance.setId(id);
    return activityInstance;
  }

  protected ActivityInstanceId createActivityInstanceId() {
    return new ActivityInstanceId(UUID.randomUUID());
  }

  public ProcessInstance signal(SignalRequest signalRequest) {
    ActivityInstanceId activityInstanceId = signalRequest.getActivityInstanceId();
    ProcessInstance processInstance = lockProcessInstanceByActivityInstanceId(activityInstanceId);
    // TODO set variables and context
    ActivityInstance activityInstance = processInstance.findActivityInstance(activityInstanceId);
    ActivityDefinition activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.signal(activityInstance);
    processInstance.executeOperations();
    return processInstance;
  }
  
  public abstract ProcessInstance lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId);

  @Override
  public ProcessDefinitionQueryBuilder buildProcessDefinitionQuery() {
    return new ProcessDefinitionQueryBuilder(this);
  }

  @Override
  public ProcessInstanceQueryBuilder buildProcessInstanceQuery() {
    return new ProcessInstanceQueryBuilder(this);
  }

  public abstract void saveProcessInstance(ProcessInstance processInstance);

  public abstract void flush(ProcessInstance processInstance);

  public abstract void flushAndUnlock(ProcessInstance processInstance);

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
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
}

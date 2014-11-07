/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fleetwood.bounder.CreateProcessInstanceRequest;
import fleetwood.bounder.ProcessDefinitionQueryBuilder;
import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.ProcessInstanceQueryBuilder;
import fleetwood.bounder.SignalRequest;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.IdVisitor;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.json.JacksonJson;
import fleetwood.bounder.json.Json;
import fleetwood.bounder.util.Exceptions;
import fleetwood.bounder.util.Log;
import fleetwood.bounder.util.Time;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl implements ProcessEngine {

  public static Log log = new Log();

  protected String id;
  protected ProcessEngine processEngine;
  protected Json json;

  public ProcessEngineImpl() {
    this.json = new JacksonJson();
  }

  public ProcessDefinition saveProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    identifyProcessDefinition(processDefinition);
    storeProcessDefinition(processDefinition);
    return processDefinition;
  }
  
  /** ensures that every element in this process definition has an id */
  protected void identifyProcessDefinition(ProcessDefinition processDefinition) {
    processDefinition.visit(new IdVisitor(this));
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
    // TODO set variables and context
    ProcessEngineImpl.log.debug("Starting "+processInstance);
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
    ProcessEngineImpl.log.debug("Locked process instance "+json.toJsonStringPretty(processInstance));
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

  public abstract void flushUpdates(ProcessInstance processInstance);

  public abstract void flushAndUnlock(ProcessInstance processInstance);

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  
  public Json getJson() {
    return json;
  }
  
  public void setJson(Json json) {
    this.json = json;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
}

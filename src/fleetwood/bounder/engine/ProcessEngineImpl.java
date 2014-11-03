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

package fleetwood.bounder.engine;

import java.util.ArrayList;
import java.util.List;

import fleetwood.bounder.CreateProcessInstanceRequest;
import fleetwood.bounder.ProcessDefinitionQueryBuilder;
import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.ProcessInstanceQuery;
import fleetwood.bounder.ProcessInstanceQueryBuilder;
import fleetwood.bounder.SignalRequest;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.CompositeDefinition;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.definition.TransitionDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.Lock;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
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

  
  public ProcessDefinition saveProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    identifyProcessDefinition(processDefinition);
    storeProcessDefinition(processDefinition);
    return processDefinition;
  }
  
  protected void identifyProcessDefinition(ProcessDefinition processDefinition) {
    if (processDefinition.getId()==null) {
      processDefinition.setId(createProcessDefinitionId(processDefinition));
    }
    identifyComposite(processDefinition);
  }

  protected void identifyComposite(CompositeDefinition compositeDefinition) {
    if (compositeDefinition.hasActivityDefinitions()) {
      for (ActivityDefinition activityDefinition: compositeDefinition.getActivityDefinitions()) {
        if (activityDefinition.getId()==null) {
          activityDefinition.setId(createActivityDefinitionId(activityDefinition));
        }
        identifyComposite(activityDefinition);
      }
    }
    if (compositeDefinition.hasTransitionDefinitions()) {
      for (TransitionDefinition transitionDefinition: compositeDefinition.getTransitionDefinitions()) {
        if (transitionDefinition.getId()==null) {
          transitionDefinition.setId(createTransitionDefinitionId(transitionDefinition));
        }
      }
    }
    if (compositeDefinition.hasVariableDefinitions()) {
      for (VariableDefinition variableDefinition: compositeDefinition.getVariableDefinitions()) {
        if (variableDefinition.getId()==null) {
          variableDefinition.setId(createVariableDefinitionId(variableDefinition));
        }
      }
    }
  }
  
  
  public ProcessInstance createProcessInstance(CreateProcessInstanceRequest createProcessInstanceRequest) {
    ProcessDefinitionId processDefinitionId = createProcessInstanceRequest.getProcessDefinitionId();
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinition processDefinition = buildProcessDefinitionQuery()
      .processDefinitionId(processDefinitionId)
      .get();
    ProcessInstanceId processInstanceId = createProcessInstanceRequest.getProcessInstanceId();
    ProcessInstance processInstance = processDefinition.createProcessInstance(processInstanceId);
    // TODO set variables and context
    ProcessEngineImpl.log.debug("Starting "+this);
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
    processInstance.setUpdates(new ArrayList<Update>());
    processInstance.executeOperations();
    return processInstance;
  }

  public ProcessInstance signal(SignalRequest signalRequest) {
    ActivityInstanceId activityInstanceId = signalRequest.getActivityInstanceId();
    ProcessInstance processInstance = lockProcessInstanceByActivityInstanceId(activityInstanceId);
    // TODO set variables and context
    ActivityInstance activityInstance = processInstance.findActivityInstance(activityInstanceId);
    ActivityDefinition activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.signal(activityInstance);
    return processInstance;
  }
  
  public ProcessInstance lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId) {
    ProcessInstanceQuery processInstanceQuery = buildProcessInstanceQuery()
      .activityInstanceId(activityInstanceId)
      .getQuery();
    processInstanceQuery.setMaxResults(1);
    List<ProcessInstance> processInstances = findProcessInstances(processInstanceQuery);
    ProcessInstance processInstance = (!processInstances.isEmpty() ? processInstances.get(0) : null);
    if (processInstance==null) { 
      throw new RuntimeException("Couldn't lock process instance");
    }
    return processInstance;
  }

  @Override
  public ProcessDefinitionQueryBuilder buildProcessDefinitionQuery() {
    return new ProcessDefinitionQueryBuilder(this);
  }

  @Override
  public ProcessInstanceQueryBuilder buildProcessInstanceQuery() {
    return new ProcessInstanceQueryBuilder(this);
  }

  protected abstract void storeProcessDefinition(ProcessDefinition processDefinition);

  public abstract ProcessDefinitionId createProcessDefinitionId(ProcessDefinition processDefinition);

  public abstract ActivityDefinitionId createActivityDefinitionId(ActivityDefinition activityDefinition);

  public abstract TransitionDefinitionId createTransitionDefinitionId(TransitionDefinition transition);

  public abstract VariableDefinitionId createVariableDefinitionId(VariableDefinition variableDefinition);

  public abstract ProcessInstanceId createProcessInstanceId(ProcessInstance processInstance);
  
  public abstract ActivityInstanceId createActivityInstanceId(ActivityInstance activityInstance);

  public abstract void saveProcessInstance(ProcessInstance processInstance);

  public abstract void flushUpdates(ProcessInstance processInstance);

  public abstract void flushUpdatesAndUnlock(ProcessInstance processInstance);

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

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

package fleetwood.bounder.engine.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fleetwood.bounder.CreateProcessInstanceRequest;
import fleetwood.bounder.ProcessDefinitionQuery;
import fleetwood.bounder.ProcessInstanceQuery;
import fleetwood.bounder.SignalRequest;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.definition.TransitionDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.engine.ProcessEngineImpl;
import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.util.Lists;


/**
 * @author Walter White
 */
public class MemoryProcessEngine extends ProcessEngineImpl {
  
  protected Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = Collections.synchronizedMap(new HashMap<ProcessDefinitionId, ProcessDefinition>());
  protected Map<ProcessInstanceId, ProcessInstance> processInstances = Collections.synchronizedMap(new HashMap<ProcessInstanceId, ProcessInstance>());
  protected Set<ProcessInstanceId> lockedProcessInstances = Collections.synchronizedSet(new HashSet<ProcessInstanceId>());
  protected long processDefinitionsCreated = 0;
  protected long activityDefinitionsCreated = 0;
  protected long transitionDefinitionsCreated = 0;
  protected long variableDefinitionsCreated = 0;
  protected long processInstancesCreated = 0;
  protected long activityInstancesCreated = 0;
  
  @Override
  protected void storeProcessDefinition(ProcessDefinition processDefinition) {
    processDefinitions.put(processDefinition.getId(), processDefinition);
    processDefinition.setProcessStore(this);
    processDefinition.prepare();
  }

  @Override
  protected void identifyProcessDefinition(ProcessDefinition processDefinition) {
    super.identifyProcessDefinition(processDefinition);
  }

  @Override
  public synchronized ActivityDefinitionId createActivityDefinitionId(ActivityDefinition activityDefinition) {
    activityDefinitionsCreated++;
    return new ActivityDefinitionId("ad"+activityDefinitionsCreated);
  }

  @Override
  public synchronized VariableDefinitionId createVariableDefinitionId(VariableDefinition variableDefinition) {
    variableDefinitionsCreated++;
    return new VariableDefinitionId("vd"+variableDefinitionsCreated);
  }

  @Override
  public synchronized TransitionDefinitionId createTransitionDefinitionId(TransitionDefinition transitionDefinition) {
    transitionDefinitionsCreated++;
    return new TransitionDefinitionId("td"+transitionDefinitionsCreated);
  }

  @Override
  public synchronized ProcessDefinitionId createProcessDefinitionId(ProcessDefinition processDefinition) {
    processDefinitionsCreated++;
    return new ProcessDefinitionId("pd"+processDefinitionsCreated);
  }

  @Override
  public synchronized ProcessInstanceId createProcessInstanceId(ProcessInstance processInstance) {
    processInstancesCreated++;
    return new ProcessInstanceId("pi"+processInstancesCreated);
  }

  @Override
  public synchronized ActivityInstanceId createActivityInstanceId(ActivityInstance activityInstance) {
    activityInstancesCreated++;
    return new ActivityInstanceId("ai"+activityInstancesCreated);
  }

  @Override
  public void saveProcessInstance(ProcessInstance processInstance) {
    processInstances.put(processInstance.getId(), processInstance);
    ProcessEngineImpl.log.debug("Saving: "+json.toJsonStringPretty(processInstance));
  }

  @Override
  public void flushUpdates(ProcessInstance processInstance) {
    ProcessEngineImpl.log.debug("Flushing updates: ");
    for (Update update: processInstance.getUpdates()) {
      ProcessEngineImpl.log.debug("  "+update);
    }
  }

  @Override
  public void flushUpdatesAndUnlock(ProcessInstance processInstance) {
    lockedProcessInstances.remove(processInstance.getId());
    processInstance.removeLock();
    flushUpdates(processInstance);
    ProcessEngineImpl.log.debug("Process instance should be: "+json.toJsonStringPretty(processInstance));
  }

  @Override
  public List<ProcessDefinition> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery) {
    if (processDefinitionQuery.getProcessDefinitionId()!=null) {
      ProcessDefinition processDefinition = processDefinitions.get(processDefinitionQuery.getProcessDefinitionId());
      return Lists.of(processDefinition);
    }
    List<ProcessDefinition> result = new ArrayList<>();
    for (ProcessDefinition processDefinition: processDefinitions.values()) {
      if (processDefinitionQuery.satisfiesCriteria(processDefinition)) {
        result.add(processDefinition);
      }
    }
    return result;
  }
  
  @Override
  public List<ProcessInstance> findProcessInstances(ProcessInstanceQuery processInstanceQuery) {
    if (processInstanceQuery.getProcessInstanceId()!=null) {
      ProcessInstance processInstance = processInstances.get(processInstanceQuery.getProcessInstanceId());
      return Lists.of(processInstance);
    }
    List<ProcessInstance> result = new ArrayList<>();
    for (ProcessInstance processInstance: processInstances.values()) {
      if (processInstanceQuery.satisfiesCriteria(processInstance)) {
        result.add(processInstance);
      }
    }
    return result;
  }
  
  public ProcessInstance lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId) {
    ProcessInstance processInstance = super.lockProcessInstanceByActivityInstanceId(activityInstanceId);
    ProcessInstanceId id = processInstance.getId();
    if (lockedProcessInstances.contains(id)) {
      throw new RuntimeException("ProcessInstance "+id+" is already locked");
    }
    lockedProcessInstances.add(id);
    return processInstance;
  }
}

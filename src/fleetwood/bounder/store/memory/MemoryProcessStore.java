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

package fleetwood.bounder.store.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.definition.TransitionDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.store.ProcessDefinitionQuery;
import fleetwood.bounder.store.ProcessInstanceQuery;
import fleetwood.bounder.store.ProcessStore;


/**
 * @author Walter White
 */
public class MemoryProcessStore extends ProcessStore {
  
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
  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return new MemoryProcessDefinitionQuery(this);
  }

  @Override
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new MemoryProcessInstanceQuery(this);
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

  public synchronized void lock(ProcessInstance processInstance, long maxWaitInMillis) {
    ProcessInstanceId id = processInstance.getId();
    if (lockedProcessInstances.contains(id)) {
      throw new RuntimeException("ProcessInstance "+id+" is already locked");
    }
    lockedProcessInstances.add(id);
  }

  @Override
  public void saveProcessInstance(ProcessInstance processInstance) {
    processInstances.put(processInstance.getId(), processInstance);
    ProcessEngine.log.debug("Saving: "+processInstance.toJson());
  }

  @Override
  public void flushUpdates(ProcessInstance processInstance) {
    ProcessEngine.log.debug("Flushing: "+processInstance.getUpdates());
  }

  @Override
  public void flushUpdatesAndUnlock(ProcessInstance processInstance) {
    ProcessEngine.log.debug("Flushing+unlock: "+processInstance.getUpdates());
    ProcessEngine.log.debug("Process instance: "+processInstance.toJson());
  }
}

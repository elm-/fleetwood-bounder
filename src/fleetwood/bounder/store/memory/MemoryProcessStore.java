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

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.store.ProcessDefinitionQuery;
import fleetwood.bounder.store.ProcessInstanceQuery;
import fleetwood.bounder.store.ProcessStore;
import fleetwood.bounder.util.Id;


/**
 * @author Tom Baeyens
 */
public class MemoryProcessStore extends ProcessStore {
  
  protected Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = Collections.synchronizedMap(new HashMap<ProcessDefinitionId, ProcessDefinition>());
  protected Map<ProcessInstanceId, ProcessInstance> processInstances = Collections.synchronizedMap(new HashMap<ProcessInstanceId, ProcessInstance>());
  protected Set<ProcessInstanceId> lockedProcessInstances = Collections.synchronizedSet(new HashSet<ProcessInstanceId>());
  protected long processDefinitionsCreated = 0;
  protected long processInstanceCreated = 0;
  
  @Override
  public void saveProcessDefinition(ProcessDefinition processDefinition) {
    processDefinition.prepare();
    processDefinitions.put(processDefinition.getId(), processDefinition);
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
  public void saveProcessInstance(ProcessInstance processInstance) {
    processInstances.put(processInstance.getId(), processInstance);
  }

  @Override
  public synchronized ProcessDefinitionId createProcessDefinitionId(ProcessDefinition processDefinition) {
    processDefinitionsCreated++;
    return new ProcessDefinitionId("pd"+processDefinitionsCreated);
  }

  @Override
  public ActivityDefinitionId createActivityDefinitionId(ActivityDefinition activityDefinition) {
    String idState = getNextIdState("ad", activityDefinition.getParent().getActivityDefinitions().keySet());
    return new ActivityDefinitionId(idState);
  }

  @Override
  public VariableDefinitionId createVariableDefinitionId(VariableDefinition variableDefinition) {
    String idState = getNextIdState("vd", variableDefinition.getParent().getVariableDefinitions().keySet());
    return new VariableDefinitionId(idState);
  }

  @Override
  public ProcessInstanceId createProcessInstanceId(ProcessInstance processInstance) {
    processInstanceCreated++;
    return new ProcessInstanceId("pi"+processInstanceCreated);
  }

  @Override
  public ActivityInstanceId createActivityInstanceId(ActivityInstance activityInstance) {
    processInstanceCreated++;
    return new ActivityInstanceId("pi"+processInstanceCreated);
  }

  private String getNextIdState(String prefix, Set<? extends Id> existingIds) {
    if (existingIds==null || existingIds.isEmpty()) {
      return prefix+1L;
    }
    Set<String> idStates = new HashSet<>();
    for (Id existingId: existingIds) {
      idStates.add((String)existingId.getState());
    }
    long index = existingIds.size()+1;
    String idState = prefix+index;
    while (idStates.contains(idState)) {
      index++;
      idState = prefix+index;
    }
    return idState;
  }

  public synchronized void lock(ProcessInstance processInstance, long maxWaitInMillis) {
    ProcessInstanceId id = processInstance.getId();
    if (lockedProcessInstances.contains(id)) {
      throw new RuntimeException("ProcessInstance "+id+" is already locked");
    }
    lockedProcessInstances.add(id);
  }
}

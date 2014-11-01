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
import java.util.Map;

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.store.ProcessDefinitionQuery;
import fleetwood.bounder.store.ProcessInstanceQuery;
import fleetwood.bounder.store.ProcessStore;


/**
 * @author Tom Baeyens
 */
public class MemoryProcessStore extends ProcessStore {
  
  protected Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = Collections.synchronizedMap(new HashMap<ProcessDefinitionId, ProcessDefinition>());
  protected Map<ProcessInstanceId, ProcessInstance> processInstancess = Collections.synchronizedMap(new HashMap<ProcessInstanceId, ProcessInstance>());
  
  @Override
  public ProcessDefinitionId saveProcessDefinition(ProcessDefinition processDefinition) {
    ProcessDefinitionId processDefinitionId = processDefinition.getId();
    if (processDefinitionId==null) {
      processDefinitionId = new ProcessDefinitionId();
      processDefinition.setId(processDefinitionId);
    }
    return processDefinitionId;
  }

  @Override
  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return new MemoryProcessDefinitionQuery(this);
  }

  @Override
  public ProcessInstance createNewProcessInstance(ProcessDefinition processDefinition) {
    ProcessInstance newProcessInstance = new ProcessInstance(this, processDefinition);
    // It is up to the ProcessStore to decide if the id is assigned now, or at the NewProcessInstance.save() 
    newProcessInstance.setId(new ProcessInstanceId());
    return newProcessInstance;
  }

  @Override
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new MemoryProcessInstanceQuery(this);
  }

  @Override
  public ProcessInstanceId saveProcessInstance(ProcessInstance processInstance) {
    ProcessInstanceId processInstanceId = processInstance.getId();
    if (processInstanceId==null) {
      processInstanceId = new ProcessInstanceId();
      processInstance.setId(processInstanceId);
    }
    return processInstanceId;
  }

  @Override
  public ProcessDefinitionId createProcessDefinitionId() {
    return new ProcessDefinitionId();
  }

  @Override
  public ActivityDefinitionId createActivityDefinitionId(ProcessDefinition processDefinition, ActivityDefinition activityDefinition) {
    return new ActivityDefinitionId();
  }

  @Override
  public VariableId createVariableDefinitionId(ProcessDefinition processDefinition, VariableDefinition variableDefinition) {
    // TODO Auto-generated method stub
    return null;
  }
}

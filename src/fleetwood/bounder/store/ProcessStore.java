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

package fleetwood.bounder.store;

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;

/**
 * @author Tom Baeyens
 */
public abstract class ProcessStore {

  public ProcessDefinition createNewProcessDefinition(ProcessDefinitionId id) {
    if (id == null) {
      id = createProcessDefinitionId();
    }
    ProcessDefinition processDefinition = new ProcessDefinition();
    processDefinition.setProcessStore(this);
    processDefinition.setId(id);
    return processDefinition;
  }

  public abstract ProcessDefinitionId createProcessDefinitionId();

  public abstract ProcessDefinitionId saveProcessDefinition(ProcessDefinition processDefinition);

  public abstract ProcessDefinitionQuery createProcessDefinitionQuery();

  public abstract ProcessInstance createNewProcessInstance(ProcessDefinition processDefinition);

  public abstract ProcessInstanceQuery createProcessInstanceQuery();

  public abstract ProcessInstanceId saveProcessInstance(ProcessInstance processInstance);

  public abstract ActivityDefinitionId createActivityDefinitionId(ProcessDefinition processDefinition, ActivityDefinition activityDefinition);

  public abstract VariableId createVariableDefinitionId(ProcessDefinition processDefinition, VariableDefinition variableDefinition);

}

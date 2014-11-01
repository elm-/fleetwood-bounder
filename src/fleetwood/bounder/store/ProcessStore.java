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
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.util.Log;

/**
 * @author Tom Baeyens
 */
public abstract class ProcessStore {
  
  public Log log = new Log();

  public ProcessDefinition createProcessDefinition(ProcessDefinitionId id) {
    ProcessDefinition processDefinition = newProcessDefinition();
    processDefinition.setProcessStore(this);
    if (id == null) {
      id = createProcessDefinitionId(processDefinition);
    }
    processDefinition.setId(id);
    return processDefinition;
  }

  protected ProcessDefinition newProcessDefinition() {
    return new ProcessDefinition();
  }

  public abstract void saveProcessDefinition(ProcessDefinition processDefinition);

  public abstract ProcessDefinitionQuery createProcessDefinitionQuery();

  public ProcessInstance createProcessInstance(ProcessDefinition processDefinition, ProcessInstanceId id) {
    ProcessInstance processInstance = newProcessInstance();
    processInstance.setProcessStore(this);
    processInstance.setProcessDefinition(processDefinition);
    processInstance.setCompositeDefinition(processDefinition);
    processInstance.setProcessInstance(processInstance);
    if (id == null) {
      id = createProcessInstanceId(processInstance);
    }
    processInstance.setId(id);
    return processInstance;
  }

  protected ProcessInstance newProcessInstance() {
    return new ProcessInstance();
  }

  public abstract ProcessInstanceQuery createProcessInstanceQuery();

  public abstract void saveProcessInstance(ProcessInstance processInstance);

  public abstract ProcessDefinitionId createProcessDefinitionId(ProcessDefinition processDefinition);

  public abstract ActivityDefinitionId createActivityDefinitionId(ActivityDefinition activityDefinition);

  public abstract VariableDefinitionId createVariableDefinitionId(VariableDefinition variableDefinition);

  public abstract ProcessInstanceId createProcessInstanceId(ProcessInstance processInstance);
  
  public abstract ActivityInstanceId createActivityInstanceId(ActivityInstance activityInstance);

}

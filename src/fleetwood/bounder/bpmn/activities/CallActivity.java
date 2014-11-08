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

package fleetwood.bounder.bpmn.activities;

import fleetwood.bounder.CreateProcessInstanceRequest;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ParameterDefinition;
import fleetwood.bounder.definition.ParameterDefinitions;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.type.Type;
import fleetwood.bounder.util.Id;


/**
 * @author Walter White
 */
public abstract class CallActivity extends ActivityDefinition {
  
  public static ParameterDefinition<Id> PROCESS_DEFINITION_ID = ParameterDefinition
    .type(Type.ID)
    .name("name");
  
  public static ParameterDefinitions PARAMETER_DEFINITIONS = new ParameterDefinitions(
    PROCESS_DEFINITION_ID);
  
  
  
  
  @Override
  public ParameterDefinitions getParameterDefinitions() {
    return PARAMETER_DEFINITIONS;
  }

  @Override
  public void start(ActivityInstance activityInstance) {
    ProcessDefinitionId processDefinitionId = (ProcessDefinitionId) PROCESS_DEFINITION_ID.get(activityInstance);
    if (processDefinitionId!=null) {
      CreateProcessInstanceRequest createProcessInstanceRequest = new CreateProcessInstanceRequest();
      createProcessInstanceRequest.setProcessDefinitionId(processDefinitionId);
      activityInstance.getProcessEngine().createProcessInstance(createProcessInstanceRequest);
    }
  }
  
  @Override
  public boolean isAsync(ActivityInstance activityInstance) {
    return true;
  }
}

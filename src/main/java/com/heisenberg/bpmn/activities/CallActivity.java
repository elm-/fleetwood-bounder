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
package com.heisenberg.bpmn.activities;

import com.heisenberg.StartProcessInstanceRequest;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ParameterDefinitionsImpl;
import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ObjectActivityParameter;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public abstract class CallActivity extends ActivityDefinitionImpl {
  
  public static ObjectActivityParameter PROCESS_DEFINITION_ID = ObjectActivityParameter
    .type(Type.ID)
    .name("name");
  
  public static ParameterDefinitionsImpl PARAMETER_DEFINITIONS = new ParameterDefinitionsImpl(
    PROCESS_DEFINITION_ID);
  
  @Override
  public ParameterDefinitionsImpl getParameterDefinitions() {
    return PARAMETER_DEFINITIONS;
  }

  @Override
  public void start(ActivityInstanceImpl activityInstance) {
    ProcessDefinitionId processDefinitionId = PROCESS_DEFINITION_ID.get(activityInstance, ProcessDefinitionId.class);
    if (processDefinitionId!=null) {
      StartProcessInstanceRequest startProcessInstanceRequest = new StartProcessInstanceRequest();
      startProcessInstanceRequest.setProcessDefinitionId(processDefinitionId);
      activityInstance.getProcessEngine().startProcessInstance(startProcessInstanceRequest);
    }
  }
  
  @Override
  public boolean isAsync(ActivityInstanceImpl activityInstance) {
    return true;
  }
}

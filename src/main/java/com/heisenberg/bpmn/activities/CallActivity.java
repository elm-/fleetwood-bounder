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

import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.id.ProcessDefinitionId;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.ObjectActivityParameter;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class CallActivity extends ActivityType {
  
  public static final String ID = "callActivity";

  @Override
  public String getId() {
    return ID;
  }
  
  public static ObjectActivityParameter PROCESS_DEFINITION_ID = ObjectActivityParameter
    .type(Type.PROCESS_DEFINITION_ID)
    .name("name");
  
  @Override
  public ActivityParameter[] getActivityParameters() {
    return new ActivityParameter[]{PROCESS_DEFINITION_ID};
  }

  @Override
  public void start(ActivityInstanceImpl activityInstance) {
    ProcessDefinitionId processDefinitionId = PROCESS_DEFINITION_ID.get(activityInstance, ProcessDefinitionId.class);
    if (processDefinitionId!=null) {
      StartProcessInstanceRequest startProcessInstanceRequest = new StartProcessInstanceRequest();
      // TODO this next line still looks ugly... should be cleaned upS
      startProcessInstanceRequest.processDefinitionRefId((String)processDefinitionId.getInternal());
      activityInstance.getProcessEngine().startProcessInstance(startProcessInstanceRequest);
    }
  }

  @Override
  public String getLabel() {
    return null;
  }
}

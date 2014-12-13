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
package com.heisenberg.api.builder;

import com.heisenberg.api.activities.ActivityType;



/**
 * @author Walter White
 */
public interface ProcessDefinitionBuilder {

  ProcessDefinitionBuilder name(String string);

  // ProcessDefinitionBuilder deployedTime(LocalDateTime deployedTime);
  
  ProcessDefinitionBuilder deployedUserId(String deployedUserId);

  ProcessDefinitionBuilder processId(String processId);
  
  ProcessDefinitionBuilder version(Long version);
  
  ProcessDefinitionBuilder organizationId(String organizationId);
  
  ProcessDefinitionBuilder line(Long lineNumber);

  ProcessDefinitionBuilder column(Long columnNumber);

  ActivityBuilder newActivity();
  ActivityBuilder newActivity(String id, ActivityType activityType);

  TransitionBuilder newTransition();

  VariableBuilder newVariable();

  TimerBuilder newTimer();
  
  DeployResult deploy();

}

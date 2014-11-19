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
package com.heisenberg.api.definition;

import com.heisenberg.definition.OrganizationId;
import com.heisenberg.definition.ProcessId;
import com.heisenberg.definition.UserId;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public interface ProcessBuilder {

  ProcessBuilder deployedTime(Long deployedTime);
  
  ProcessBuilder deployedUserId(UserId deployedUserId);

  ProcessBuilder processId(ProcessId processId);
  
  ProcessBuilder version(Long version);
  
  ProcessBuilder organizationId(OrganizationId organizationId);
  
  ProcessBuilder type(Type type);

  ProcessBuilder name(String processDefinitionName);

  ProcessBuilder line(Long lineNumber);

  ProcessBuilder column(Long columnNumber);

  ActivityBuilder newActivity();

  TransitionBuilder newTransition();

  VariableBuilder newVariable();

  TimerBuilder newTimer();
}

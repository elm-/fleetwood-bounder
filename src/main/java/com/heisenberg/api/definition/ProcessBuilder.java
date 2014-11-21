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

import org.joda.time.LocalDateTime;

import com.heisenberg.api.id.OrganizationId;
import com.heisenberg.api.id.ProcessId;
import com.heisenberg.api.id.UserId;
import com.heisenberg.spi.DataType;


/**
 * @author Walter White
 */
public interface ProcessBuilder {

  ProcessBuilder deployedTime(LocalDateTime deployedTime);
  
  ProcessBuilder deployedUserId(UserId deployedUserId);

  ProcessBuilder processId(ProcessId processId);
  
  ProcessBuilder version(Long version);
  
  ProcessBuilder organizationId(OrganizationId organizationId);
  
  ProcessBuilder dataType(DataType dataType);

  ProcessBuilder name(String processDefinitionName);

  ProcessBuilder line(Long lineNumber);

  ProcessBuilder column(Long columnNumber);

  ActivityBuilder newActivity();

  TransitionBuilder newTransition();

  VariableBuilder newVariable();

  TimerBuilder newTimer();
}

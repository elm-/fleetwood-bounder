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
package com.heisenberg.api;

import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.builder.WorkflowQuery;
import com.heisenberg.api.builder.WorkflowInstanceQuery;
import com.heisenberg.api.builder.StartBuilder;


/** Start here.
 * 
 * Obtain a process engine by instantiating one of the concrete classes like this:
 * 
 * <pre>{@code
 * ProcessEngine processEngine = new MemoryProcessEngine();
 * }</pre>
 * 
 * @author Walter White
 */
public interface WorkflowEngine {
  
  DataTypes getDataTypes();
  
  /** Start building a new process, when done, call {@link WorkflowBuilder#deploy()} */
  WorkflowBuilder newWorkflow();

  /** Use a {@link StartBuilder trigger} to start a new process instance for a process definition. */
  StartBuilder newStart();

  /** Use a {@link MessageBuilder message} to end a waiting activity instance in a process instance. */
  MessageBuilder newMessage();

  WorkflowInstanceQuery newProcessInstanceQuery();
  
  WorkflowQuery newProcessDefinitionQuery();
}

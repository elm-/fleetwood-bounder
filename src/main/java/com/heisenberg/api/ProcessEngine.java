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

import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.instance.ProcessInstance;


/** Start here.
 * 
 * Obtain a process engine by instantiating one of the concrete classes like this:
 *   ProcessEngine processEngine = new MemoryProcessEngine();
 * 
 * @author Walter White
 */
public interface ProcessEngine {
  
  ProcessBuilder newProcess();
  
  /** potentially changes the passed processDefinition (assigning ids) 
   * and returns the same object as a way to indicate it may have changed. */
  DeployProcessDefinitionResponse deployProcessDefinition(ProcessBuilder processDefinition);
  
  // TODO change response into StartProcessInstanceResponse
  //      this way we can include the events/logs of all things that happened during execution.
  ProcessInstance startProcessInstance(StartProcessInstanceRequest startProcessInstanceRequest);
  
  ActivityInstanceQuery createActivityInstanceQuery(); 

  // TODO change response into SignalResponse
  //      this way we can include the events/logs of all things that happened during execution.
  ProcessInstance signal(SignalRequest signalRequest);
}

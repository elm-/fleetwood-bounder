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
package com.heisenberg;

import java.util.List;

import com.heisenberg.definition.ProcessDefinition;
import com.heisenberg.instance.ProcessInstance;



/**
 * @author Walter White
 */
public interface ProcessEngine {
  
  /** potentially changes the passed processDefinition (assigning ids) 
   * and returns the same object as a way to indicate it may have changed. */
  ProcessDefinition saveProcessDefinition(ProcessDefinition processDefinition);
  
  ProcessDefinitionQueryBuilder buildProcessDefinitionQuery();
  List<ProcessDefinition> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery);

  ProcessInstanceQueryBuilder buildProcessInstanceQuery();
  List<ProcessInstance> findProcessInstances(ProcessInstanceQuery processInstanceQuery);

  ProcessInstance createProcessInstance(CreateProcessInstanceRequest createProcessInstanceRequest);

  ProcessInstance signal(SignalRequest signalRequest);
}

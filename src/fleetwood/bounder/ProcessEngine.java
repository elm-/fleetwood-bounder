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

package fleetwood.bounder;

import java.util.List;

import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;



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

  ProcessInstance createProcessInstance(ProcessDefinitionId processDefinitionId);
  
  ProcessInstance createProcessInstance(ProcessDefinitionId processDefinitionId, ProcessInstanceId processInstanceId);
  
}

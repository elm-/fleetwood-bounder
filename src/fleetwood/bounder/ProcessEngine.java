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

import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.store.ProcessDefinitionQuery;
import fleetwood.bounder.store.ProcessInstanceQuery;
import fleetwood.bounder.store.ProcessStore;
import fleetwood.bounder.util.Exceptions;
import fleetwood.bounder.util.Log;



/**
 * @author Tom Baeyens
 */
public class ProcessEngine {
  
  ProcessStore processStore;

  public ProcessDefinition createNewProcessDefinition() {
    return createNewProcessDefinition(null);
  }

  public ProcessDefinition createNewProcessDefinition(ProcessDefinitionId id) {
    return processStore.createProcessDefinition(id);
  }

  public void saveProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    processStore.saveProcessDefinition(processDefinition);
  }
  
  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return processStore.createProcessDefinitionQuery();
  }

  public ProcessInstance createProcessInstance(ProcessDefinitionId processDefinitionId) {
    return createProcessInstance(processDefinitionId, null);
  }
  
  public ProcessInstance createProcessInstance(ProcessDefinitionId processDefinitionId, ProcessInstanceId processInstanceId) {
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinition processDefinition = createProcessDefinitionQuery()
      .id(processDefinitionId)
      .get();
    return processStore.createProcessInstance(processDefinition, processInstanceId);
  }

  public ProcessInstanceQuery createProcessInstanceQuery() {
    return processStore.createProcessInstanceQuery();
  }
  
  public ProcessStore getProcessStore() {
    return processStore;
  }
  
  public void setProcessStore(ProcessStore processStore) {
    this.processStore = processStore;
  }
}

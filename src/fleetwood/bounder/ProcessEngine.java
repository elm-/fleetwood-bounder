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
import fleetwood.bounder.json.Json;
import fleetwood.bounder.store.ProcessDefinitionQuery;
import fleetwood.bounder.store.ProcessInstanceQuery;
import fleetwood.bounder.store.ProcessStore;
import fleetwood.bounder.util.Exceptions;
import fleetwood.bounder.util.Log;



/**
 * @author Walter White
 */
public class ProcessEngine {
  
  public static Log log = new Log();

  ProcessStore processStore;
  Json json;

  /** potentially changes the passed processDefinition (assigning ids) 
   * and returns the same object as a way to indicate it may have changed. */
  public ProcessDefinition saveProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    processStore.saveProcessDefinition(processDefinition);
    return processDefinition;
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
    return processDefinition.createProcessInstance(processInstanceId);
  }

  public ProcessInstanceQuery createProcessInstanceQuery() {
    return processStore.createProcessInstanceQuery();
  }
  
  public ProcessStore getProcessStore() {
    return processStore;
  }
  
  public void setProcessStore(ProcessStore processStore) {
    this.processStore = processStore;
    this.processStore.setProcessEngine(this);
  }

  public Json getJson() {
    return json;
  }

  public void setJson(Json json) {
    this.json = json;
  }
}

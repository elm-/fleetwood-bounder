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

package fleetwood.bounder.definition;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.instance.ProcessInstanceUpdates;


/**
 * @author Walter White
 */
public class ProcessDefinition extends CompositeDefinition {

  protected ProcessDefinitionId id;

  public void prepare() {
    this.processDefinition = this;
    super.prepare();
  }

  public ProcessDefinitionId getId() {
    return id;
  }
  
  public void setId(ProcessDefinitionId id) {
    this.id = id;
  }

  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }

  public ProcessInstance createProcessInstance(ProcessInstanceId processInstanceId) {
    ProcessInstance processInstance = new ProcessInstance();
    processInstance.setProcessStore(processStore);
    processInstance.setProcessDefinition(this);
    processInstance.setCompositeDefinition(this);
    processInstance.setProcessInstance(processInstance);
    if (processInstanceId == null) {
      processInstanceId = processStore.createProcessInstanceId(processInstance);
    }
    processInstance.setId(processInstanceId);
    ProcessEngine.log.debug("Created "+processInstance);
    return processInstance;
  }
}

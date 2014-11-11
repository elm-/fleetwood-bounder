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

import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.instance.ProcessInstanceId;


/**
 * @author Walter White
 */
public class CreateProcessInstanceRequest extends VariableRequest {

  protected ProcessDefinitionId processDefinitionId;
  protected ProcessInstanceId processInstanceId;
  
  public ProcessDefinitionId getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(ProcessDefinitionId processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
  
  public ProcessInstanceId getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(ProcessInstanceId processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
}

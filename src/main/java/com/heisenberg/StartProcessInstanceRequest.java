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

import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.definition.VariableDefinitionId;
import com.heisenberg.instance.ProcessInstanceId;


/**
 * @author Walter White
 */
public class StartProcessInstanceRequest extends VariableRequest {

  protected ProcessDefinitionId processDefinitionId;
  protected ProcessInstanceId processInstanceId;
  
  public StartProcessInstanceRequest processDefinitionId(ProcessDefinitionId processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public StartProcessInstanceRequest variableValue(VariableDefinitionId variableDefinitionId, Object value) {
    super.variableValue(variableDefinitionId, value);
    return this;
  }
  
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

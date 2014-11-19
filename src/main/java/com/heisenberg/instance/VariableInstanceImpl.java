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
package com.heisenberg.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.definition.VariableDefinitionImpl;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class VariableInstanceImpl implements VariableInstance {

  @JsonIgnore
  public ProcessEngineImpl processEngine;
  @JsonIgnore
  public ScopeInstanceImpl parent;
  @JsonIgnore
  public ProcessInstanceImpl processInstance;
  
  @JsonIgnore
  public VariableDefinitionImpl variableDefinition;
  @JsonIgnore
  public Type type;

  public Object value;
  public String typeId;
  public String variableDefinitionName;

  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public ScopeInstanceImpl getParent() {
    return parent;
  }
  
  public void setParent(ScopeInstanceImpl parent) {
    this.parent = parent;
  }
  
  public ProcessInstanceImpl getProcessInstance() {
    return processInstance;
  }
  
  public void setProcessInstance(ProcessInstanceImpl processInstance) {
    this.processInstance = processInstance;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public VariableDefinitionImpl getVariableDefinition() {
    return variableDefinition;
  }
  
  public void setVariableDefinition(VariableDefinitionImpl variableDefinition) {
    this.variableDefinition = variableDefinition;
  }

  @Override
  public String getVariableDefinitionName() {
    return variableDefinitionName;
  }

  @Override
  public String getTypeId() {
    return typeId;
  }
}

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
package com.heisenberg.impl.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.api.type.DataType;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;


/**
 * @author Walter White
 */
public class VariableInstanceImpl implements VariableInstance {

  public String id;
  @JsonIgnore
  public ProcessEngineImpl processEngine;
  @JsonIgnore
  public ScopeInstanceImpl parent;
  @JsonIgnore
  public ProcessInstanceImpl processInstance;
  
  @JsonIgnore
  public VariableDefinitionImpl variableDefinition;
  @JsonIgnore
  public DataType dataType;

  public Object value;
  public String variableDefinitionId;

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

  public DataType getType() {
    return dataType;
  }

  public void setType(DataType dataType) {
    this.dataType = dataType;
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
  public String getVariableDefinitionId() {
    return variableDefinitionId;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  
  public DataType getDataType() {
    return dataType;
  }

  
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public void setVariableDefinitionId(String variableDefinitionId) {
    this.variableDefinitionId = variableDefinitionId;
  }
}

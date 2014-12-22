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
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.definition.VariableImpl;
import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public class VariableInstanceImpl implements VariableInstance {

  public String id;
  public Object value;
  public String variableDefinitionId;

  @JsonIgnore
  public WorkflowEngineImpl processEngine;
  @JsonIgnore
  public ScopeInstanceImpl parent;
  @JsonIgnore
  public WorkflowInstanceImpl processInstance;
  @JsonIgnore
  public VariableImpl variableDefinition;
  @JsonIgnore
  public DataType dataType;
  @JsonIgnore
  public VariableInstanceUpdates updates;

  public WorkflowEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(WorkflowEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public ScopeInstanceImpl getParent() {
    return parent;
  }
  
  public void setParent(ScopeInstanceImpl parent) {
    this.parent = parent;
  }
  
  public WorkflowInstanceImpl getProcessInstance() {
    return processInstance;
  }
  
  public void setProcessInstance(WorkflowInstanceImpl processInstance) {
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
    if (updates!=null) {
      updates.isValueChanged = true;
    }
  }
  
  public VariableImpl getVariableDefinition() {
    return variableDefinition;
  }
  
  public void setVariableDefinition(VariableImpl variableDefinition) {
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

  public void trackUpdates(boolean isNew) {
    updates = new VariableInstanceUpdates(isNew);
  }
}

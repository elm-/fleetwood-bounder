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
package com.heisenberg.definition;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.definition.VariableBuilder;
import com.heisenberg.api.definition.VariableDefinition;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.VariableInstanceImpl;
import com.heisenberg.spi.DataType;


/**
 * @author Walter White
 */
public class VariableDefinitionImpl implements VariableBuilder, VariableDefinition {

  public String name;
  public String dataTypeId;
  public Map<String,Object> dataTypeJsonMap;
  public DataType dataType;
  public Object initialValue;

  @JsonIgnore
  public ProcessEngineImpl processEngine;
  @JsonIgnore
  public ProcessDefinitionImpl processDefinition;  
  @JsonIgnore
  public ScopeDefinitionImpl parent;

  public Long line;
  public Long column;

  public VariableDefinitionImpl name(String name) {
    this.name = name;
    return this;
  }

  public VariableDefinitionImpl line(Long line) {
    this.line = line;
    return this;
  }

  public VariableDefinitionImpl column(Long column) {
    this.column = column;
    return this;
  }

  public VariableDefinitionImpl type(String typeId) {
    this.dataTypeId = typeId;
    return this;
  }

  public VariableDefinitionImpl dataTypeId(String dataTypeId) {
    this.dataTypeId = dataTypeId;
    return this;
  }

  /** this class has to be registered with @link {@link ProcessEngineImpl#registerJavaBeanType(Class)} */
  public VariableDefinitionImpl dataTypeJavaBean(Class<?> userDefinedJavaBeanClass) {
    this.dataTypeId = userDefinedJavaBeanClass.getName();
    return this;
  }

  public VariableDefinitionImpl dataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }

  public VariableDefinitionImpl dataTypeJsonMap(Map<String,Object> dataTypeJsonMap) {
    this.dataTypeJsonMap = dataTypeJsonMap;
    return this;
  }

  public VariableDefinitionImpl initialValue(Object initialValue) {
    this.initialValue = initialValue;
    return this;
  }
  
  public void prepare() {
  }
  
  public ScopeDefinitionImpl getParent() {
    return parent;
  }

  public void setParent(ScopeDefinitionImpl parent) {
    this.parent = parent;
  }

  public String getName() {
    return name;
  }
  
  public VariableDefinitionImpl setName(String name) {
    this.name = name;
    return this;
  }
  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
  
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public DataType getType() {
    return dataType;
  }
  
  public void setType(DataType dataType) {
    this.dataType = dataType;
  }

  public VariableInstanceImpl createVariableInstance() {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.setType(dataType);
    variableInstance.setVariableDefinition(this);
    variableInstance.setValue(initialValue);
    return variableInstance;
  }
}

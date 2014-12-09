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
package com.heisenberg.impl.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.builder.VariableBuilder;
import com.heisenberg.api.definition.VariableDefinition;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.DataTypeReference;
import com.heisenberg.impl.AbstractProcessEngine;
import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class VariableDefinitionImpl implements VariableBuilder, VariableDefinition {

  public String id;
  
//  public String dataTypeId;
//  @JsonIgnore
  public DataType dataType;
//  @JsonProperty("dataType")
//  public Map<String,Object> dataTypeJson;
  
  @JsonIgnore
  public Object initialValue;
//  @JsonProperty("initialValue")
//  public Object initialValueJson;

  @JsonIgnore
  public AbstractProcessEngine processEngine;
  @JsonIgnore
  public ProcessDefinitionImpl processDefinition;  
  @JsonIgnore
  public ScopeDefinitionImpl parent;

  public Long line;
  public Long column;

  public VariableDefinitionImpl id(String id) {
    this.id = id;
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

  /** this class has to be registered with @link {@link ProcessEngineImpl#registerJavaBeanType(Class)} */
  public VariableDefinitionImpl dataTypeJavaBean(Class<?> userDefinedJavaBeanClass) {
    this.dataType = new DataTypeReference(userDefinedJavaBeanClass.getName());
    return this;
  }
  
  public VariableDefinitionImpl dataType(DataType dataType) {
    this.dataType = dataType;
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

  public String getId() {
    return id;
  }
  
  public VariableDefinitionImpl setId(String id) {
    this.id = id;
    return this;
  }
  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
  
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  public AbstractProcessEngine getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(AbstractProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
  
  public DataType getType() {
    return dataType;
  }
  
  public void setType(DataType dataType) {
    this.dataType = dataType;
  }
}

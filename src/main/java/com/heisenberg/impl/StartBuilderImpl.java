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
package com.heisenberg.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.type.DataType;




/**
 * @author Walter White
 */
public class StartBuilderImpl extends VariableRequestImpl implements StartBuilder {

  @JsonIgnore
  public ProcessEngineImpl processEngine;
  public String processDefinitionId;
  public String processDefinitionName;
  public String callerProcessInstanceId;
  public String callerActivityInstanceId;

  public StartBuilderImpl() {
  }

  public StartBuilderImpl(ProcessEngineImpl processEngine, JsonService jsonService) {
    super(jsonService);
    this.processEngine = processEngine;
  }


  @Override
  public StartBuilderImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public StartBuilderImpl processDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
    return this;
  }

  @Override
  public StartBuilderImpl variableValue(String variableDefinitionId, Object internalValue) {
    super.variableValue(variableDefinitionId, internalValue);
    return this;
  }

  @Override
  public StartBuilderImpl variableValue(String variableDefinitionId, Object jsonValue, DataType dataType) {
    super.variableValue(variableDefinitionId, jsonValue, dataType);
    return this;
  }

  @Override
  public StartBuilderImpl variableValue(String variableDefinitionId, Object value, Class<?> javaBeanType) {
    super.variableValue(variableDefinitionId, value, javaBeanType);
    return this;
  }

  @Override
  public StartBuilderImpl transientContext(String key, Object value) {
    super.transientContext(key, value);
    return this;
  }

  @Override
  public ProcessInstance startProcessInstance() {
    return processEngine.startProcessInstance(this);
  }
}

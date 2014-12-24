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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.type.DataType;




/**
 * @author Walter White
 */
public class StartImpl extends VariableRequestImpl implements StartBuilder {

  @JsonIgnore
  public WorkflowEngineImpl processEngine;
  public String processDefinitionId;
  public String processDefinitionName;
  public String callerWorkflowInstanceId;
  public String callerActivityInstanceId;

  public StartImpl() {
  }

  public StartImpl(WorkflowEngineImpl processEngine, JsonService jsonService) {
    super(jsonService);
    this.processEngine = processEngine;
  }


  @Override
  public StartImpl workflowId(String workflowId) {
    this.processDefinitionId = workflowId;
    return this;
  }

  @Override
  public StartImpl workflowName(String workflowName) {
    this.processDefinitionName = workflowName;
    return this;
  }

  @Override
  public StartImpl variableValue(String variableDefinitionId, Object internalValue) {
    super.variableValue(variableDefinitionId, internalValue);
    return this;
  }

  @Override
  public StartImpl variableValue(String variableDefinitionId, Object jsonValue, DataType dataType) {
    super.variableValue(variableDefinitionId, jsonValue, dataType);
    return this;
  }

  @Override
  public StartImpl variableValue(String variableDefinitionId, Object value, Class<?> javaBeanType) {
    super.variableValue(variableDefinitionId, value, javaBeanType);
    return this;
  }

  @Override
  public StartImpl transientContext(String key, Object value) {
    super.transientContext(key, value);
    return this;
  }

  @Override
  public StartImpl transientContext(Map<String,Object> transientContext) {
    super.transientContext(transientContext);
    return this;
  }

  @Override
  public WorkflowInstance startWorkflowInstance() {
    return processEngine.startWorkflowInstance(this);
  }
}

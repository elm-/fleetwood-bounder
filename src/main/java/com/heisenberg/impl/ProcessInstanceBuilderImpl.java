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

import com.heisenberg.api.ProcessInstanceBuilder;
import com.heisenberg.api.instance.ProcessInstance;




/**
 * @author Walter White
 */
public class ProcessInstanceBuilderImpl extends VariableRequestImpl implements ProcessInstanceBuilder {

  public String processDefinitionId;

//  public ProcessInstanceBuilderImpl() {
//  }

  public ProcessInstanceBuilderImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
  }


  @Override
  public ProcessInstanceBuilderImpl processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public ProcessInstanceBuilderImpl variableValue(String variableDefinitionIdInternal, Object value) {
    super.variableValue(variableDefinitionIdInternal, value);
    return this;
  }

  @Override
  public ProcessInstanceBuilderImpl variableValueJson(String variableDefinitionIdInternal, Object valueJson) {
    super.variableValueJson(variableDefinitionIdInternal, valueJson);
    return this;
  }

  @Override
  public ProcessInstanceBuilderImpl transientContext(String key, Object value) {
    super.transientContext(key, value);
    return this;
  }

  @Override
  public ProcessInstance start() {
    return processEngine.startProcessInstance(this);
  }
}

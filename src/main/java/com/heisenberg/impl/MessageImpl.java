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
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.type.DataType;



/**
 * @author Walter White
 */
public class MessageImpl extends VariableRequestImpl implements MessageBuilder {

  @JsonIgnore
  protected WorkflowEngineImpl processEngine;
  public String activityInstanceId;
  public String processInstanceId;

  public MessageImpl() {
  }

  public MessageImpl(WorkflowEngineImpl processEngine, JsonService jsonService) {
    super(jsonService);
    this.processEngine = processEngine;
  }

  @Override
  public MessageImpl activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }
  
  @Override
  public MessageImpl processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public Object getActivityInstanceId() {
    return activityInstanceId;
  }
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
  
  @Override
  public MessageImpl variableValue(String variableDefinitionIdInternal, Object value) {
    super.variableValue(variableDefinitionIdInternal, value);
    return this;
  }
  
  @Override
  public MessageImpl variableValue(String variableDefinitionId, Object value, DataType dataType) {
    super.variableValue(variableDefinitionId, value, dataType);
    return this;
  }

  @Override
  public MessageImpl variableValue(String variableDefinitionId, Object value, Class<?> javaBeanType) {
    super.variableValue(variableDefinitionId, value, javaBeanType);
    return this;
  }

  @Override
  public MessageImpl transientContext(String key, Object value) {
    super.transientContext(key, value);
    return this;
  }
  
  @Override
  public WorkflowInstanceImpl send() {
    return processEngine.sendActivityInstanceMessage(this);
  }
}

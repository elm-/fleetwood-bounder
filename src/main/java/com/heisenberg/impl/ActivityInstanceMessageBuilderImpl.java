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

import com.heisenberg.api.ActivityInstanceMessageBuilder;
import com.heisenberg.impl.instance.ProcessInstanceImpl;



/**
 * @author Walter White
 */
public class ActivityInstanceMessageBuilderImpl extends VariableRequestImpl implements ActivityInstanceMessageBuilder {

  public String activityInstanceId;
  public String processInstanceId;

  public ActivityInstanceMessageBuilderImpl(ProcessEngineImpl processEngine) {
    super(processEngine);
  }

  @Override
  public ActivityInstanceMessageBuilderImpl activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }
  
  @Override
  public ActivityInstanceMessageBuilderImpl processInstanceId(String processInstanceId) {
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
  public ActivityInstanceMessageBuilderImpl variableValue(String variableDefinitionIdInternal, Object value) {
    super.variableValue(variableDefinitionIdInternal, value);
    return this;
  }

  @Override
  public ActivityInstanceMessageBuilderImpl variableValueJson(String variableDefinitionIdInternal, Object valueJson) {
    super.variableValueJson(variableDefinitionIdInternal, valueJson);
    return this;
  }

  @Override
  public ActivityInstanceMessageBuilderImpl transientContext(String key, Object value) {
    super.transientContext(key, value);
    return this;
  }
  
  @Override
  public ProcessInstanceImpl send() {
    return processEngine.sendActivityInstanceMessage(this);
  }
}

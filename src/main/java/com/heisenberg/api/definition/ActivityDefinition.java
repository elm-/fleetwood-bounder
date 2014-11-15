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
package com.heisenberg.api.definition;

import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class ActivityDefinition extends ScopeDefinition {
  
  public String activityTypeRefId;
  
  public ActivityDefinition type(String activityTypeRefId) {
    this.activityTypeRefId = activityTypeRefId;
    return this;
  }
  
  public ActivityDefinition type(Type type) {
    type(type.getId());
    return this;
  }
  
  @Override
  public ActivityDefinition name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public ActivityDefinition activity(ActivityDefinition activityDefinition) {
    super.activity(activityDefinition);
    return this;
  }

  @Override
  public ActivityDefinition transition(ActivityDefinition fromActivityDefinition, ActivityDefinition toActivityDefinition) {
    super.transition(fromActivityDefinition, toActivityDefinition);
    return this;
  }

  @Override
  public ActivityDefinition transition(String fromActivityDefinitionName, String toActivityDefinitionName) {
    super.transition(fromActivityDefinitionName, toActivityDefinitionName);
    return this;
  }

  @Override
  public ActivityDefinition transition(TransitionDefinition transition) {
    super.transition(transition);
    return this;
  }

  @Override
  public ActivityDefinition variable(VariableDefinition variableDefinition) {
    super.variable(variableDefinition);
    return this;
  }

  @Override
  public ActivityDefinition timer(TimerDefinition timerDefinition) {
    super.timer(timerDefinition);
    return this;
  }

  @Override
  public ActivityDefinition parameterValue(ActivityParameter activityParameter, Object object) {
    super.parameterValue(activityParameter, object);
    return this;
  }

  @Override
  public ActivityDefinition parameterExpression(ActivityParameter activityParameter, String expression) {
    super.parameterExpression(activityParameter, expression);
    return this;
  }

  @Override
  public ActivityDefinition parameterVariable(ActivityParameter activityParameter, String variableDefinitionRefName) {
    super.parameterVariable(activityParameter, variableDefinitionRefName);
    return this;
  }
}

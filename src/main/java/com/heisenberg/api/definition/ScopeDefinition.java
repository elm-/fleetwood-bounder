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

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public class ScopeDefinition {

  public String name;
  public List<ParameterInstance> parameterInstances;
  public List<ActivityDefinition> activityDefinitions;
  public List<VariableDefinition> variableDefinitions;
  public List<TransitionDefinition> transitionDefinitions;
  public List<TimerDefinition> timerDefinitions;
  
  public ScopeDefinition name(String name) {
    this.name = name;
    return this;
  }
  
  public ScopeDefinition activity(ActivityDefinition activityDefinition) {
    if (activityDefinitions==null) {
      activityDefinitions = new ArrayList<ActivityDefinition>();
    }
    activityDefinitions.add(activityDefinition);
    return this;
  }

  public ScopeDefinition transition(ActivityDefinition fromActivityDefinition, ActivityDefinition toActivityDefinition) {
    return transition(fromActivityDefinition.name, toActivityDefinition.name);
  }

  public ScopeDefinition transition(String fromActivityDefinitionName, String toActivityDefinitionName) {
    Exceptions.checkNotNull(fromActivityDefinitionName, "From activity definition does not have a name");
    Exceptions.checkNotNull(toActivityDefinitionName, "To activity definition does not have a name");
    transition(new TransitionDefinition()
      .from(fromActivityDefinitionName)
      .to(toActivityDefinitionName)
    );
    return this;
  }

  public ScopeDefinition transition(TransitionDefinition transition) {
    if (transitionDefinitions==null) {
      transitionDefinitions = new ArrayList<TransitionDefinition>();
    }
    transitionDefinitions.add(transition);
    return this;
  }

  public ScopeDefinition variable(VariableDefinition variableDefinition) {
    if (variableDefinitions==null) {
      variableDefinitions = new ArrayList<VariableDefinition>();
    }
    variableDefinitions.add(variableDefinition);
    return this;
  }
  
  public ScopeDefinition timer(TimerDefinition timerDefinition) {
    if (timerDefinitions==null) {
      timerDefinitions = new ArrayList<TimerDefinition>();
    }
    timerDefinitions.add(timerDefinition);
    return this;
  }
  
  public  ScopeDefinition parameterValue(ActivityParameter activityParameter, Object object) {
    addParameterValue(activityParameter.name, new ParameterBinding().value(object));
    return this;
  }
  public  ScopeDefinition parameterExpression(ActivityParameter activityParameter, String expression) {
    addParameterValue(activityParameter.name, new ParameterBinding().expression(expression));
    return this;
  }
  public  ScopeDefinition parameterVariable(ActivityParameter activityParameter, Object variableDefinitionId) {
    addParameterValue(activityParameter.name, new ParameterBinding().variableDefinitionId(variableDefinitionId));
    return this;
  }

  void addParameterValue(String parameterRefId, ParameterBinding parameterBinding) {
    Exceptions.checkNotNull(parameterRefId, "parameterRefId");
    Exceptions.checkNotNull(parameterBinding, "parameterBinding");
    ParameterInstance parameterInstance = findParameterInstance(parameterRefId);
    if (parameterInstance==null) {
      parameterInstance = new ParameterInstance()
        .parameterRefId(parameterRefId);
      if (parameterInstances==null) {
        parameterInstances = new ArrayList<>();
      }
      parameterInstances.add(parameterInstance);
    }
    parameterInstance.parameterBinding(parameterBinding);
  }

  ParameterInstance findParameterInstance(String parameterRefId) {
    if (parameterInstances!=null) {
      for (ParameterInstance parameterInstance: parameterInstances) {
        if (parameterRefId.equals(parameterRefId)) {
          return parameterInstance;
        }
      }
    }
    return null;
  }
}

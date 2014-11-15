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

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.definition.ParameterBinding;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.type.TypedValue;


/**
 * @author Walter White
 */
public class ParameterBindingImpl {

  // one of the next 3 specifies the value
  protected Object value;
  protected VariableDefinitionImpl variableDefinition;
  protected String expression;
  
  public ParameterBindingImpl value(Object value) {
    this.value = value;
    return this;
  }
  
  public ParameterBindingImpl expression(String expression) {
    this.expression = expression;
    return this;
  }
  
  public ParameterBindingImpl variableDefinition(VariableDefinitionImpl variableDefinition) {
    this.variableDefinition = variableDefinition;
    return this;
  }

  public Object getValue() {
    return value;
  }
  
  public void setValue(Object object) {
    this.value = object;
  }
  
  public VariableDefinitionImpl getVariableDefinition() {
    return variableDefinition;
  }
  
  public void setVariableDefinition(VariableDefinitionImpl variableDefinition) {
    this.variableDefinition = variableDefinition;
  }
  
  public String getExpression() {
    return expression;
  }
  
  public void setExpression(String expression) {
    this.expression = expression;
  }

  public Object getValue(ActivityInstanceImpl activityInstance) {
    if (value!=null) {
      return value;
    }
    if (variableDefinition!=null) {
      return activityInstance.getVariableValueRecursive(variableDefinition.name).getValue();
    }
    if (expression!=null) {
      // TODO
      return null;
    }
    return null;
  }

  public void parse(ProcessEngineImpl processEngine, DeployProcessDefinitionResponse response, ProcessDefinitionImpl processDefinition,
          ScopeDefinitionImpl parentScopeDefinition, ParameterInstanceImpl parentParameterInstance, ParameterBinding parameterBinding) {
    if (parameterBinding.variableDefinitionRefName!=null) {
      variableDefinition = parentScopeDefinition.findVariableDefinitionByName(parameterBinding.variableDefinitionRefName);
      
    }
  }
}

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
import com.heisenberg.expressions.Script;
import com.heisenberg.expressions.ScriptResult;
import com.heisenberg.expressions.Scripts;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.InvalidApiValueException;


/**
 * @author Walter White
 */
public class ParameterBindingImpl {

  // one of the next 3 specifies the value
  protected Object value;
  protected VariableDefinitionImpl variableDefinition;
  protected Script expression;
  
  public ParameterBindingImpl value(Object value) {
    this.value = value;
    return this;
  }
  
  public ParameterBindingImpl expression(Script expression) {
    this.expression = expression;
    return this;
  }
  
  public ParameterBindingImpl variableDefinition(VariableDefinitionImpl variableDefinition) {
    this.variableDefinition = variableDefinition;
    return this;
  }

  public Object getValue(ActivityInstanceImpl activityInstance) {
    if (value!=null) {
      return value;
    }
    if (variableDefinition!=null) {
      return activityInstance.getVariableValueRecursive(variableDefinition.name).getValue();
    }
    if (expression!=null) {
      ScriptResult result = activityInstance.processEngine.scripts.evaluateScript(activityInstance, expression);
      return result.getResult();
    }
    return null;
  }

  public void parse(ProcessEngineImpl processEngine, DeployProcessDefinitionResponse response, ProcessDefinitionImpl processDefinition,
          ScopeDefinitionImpl scopeDefinitionImpl, ParameterInstanceImpl parentParameterInstance, ActivityParameter activityParameter, ParameterBinding parameterBinding) {
    int valueSpecifications = 0;
    if (parameterBinding.value!=null) {
      try {
        value = activityParameter.type.convertApiToInternalValue(parameterBinding.value);
      } catch (InvalidApiValueException e) {
        response.addError(parameterBinding.location, "Couldn't parse parameter %s binding value %s as a %s: %s", parentParameterInstance.name, parameterBinding.value, activityParameter.type, e.getMessage());
      }
      valueSpecifications++;
    }
    if (parameterBinding.variableDefinitionRefName!=null) {
      variableDefinition = scopeDefinitionImpl.findVariableDefinitionByName(parameterBinding.variableDefinitionRefName);
      if (!variableDefinition.type.equals(activityParameter.type)) {
        response.addError(parameterBinding.location, "Variable %s (%s) can't be bound to parameter %s (%s) because the types don't match", 
                parameterBinding.variableDefinitionRefName, 
                variableDefinition.type.getId(), 
                parameterBinding.variableDefinitionRefName,
                variableDefinition.type.getId());
      }
      valueSpecifications++;
    }
    if (parameterBinding.expression!=null) {
      String language = parameterBinding.expressionLanguage != null ? parameterBinding.expressionLanguage : Scripts.JAVASCRIPT;
      try {
        expression = processEngine.scripts.compile(parameterBinding.expression, language);
      } catch (Exception e) {
        response.addError(parameterBinding.location, "Couldn't compile %s expression: %s: %s", language, e.getMessage(), parameterBinding.expression);
      }
      valueSpecifications++;
    }
    if (valueSpecifications==0) {
      response.addWarning(parameterBinding.location, "No value, variableDefinitionName or expression specified");
    }
    if (valueSpecifications>1) {
      response.addWarning(parameterBinding.location, "More then one value specified");
    }
  }
}

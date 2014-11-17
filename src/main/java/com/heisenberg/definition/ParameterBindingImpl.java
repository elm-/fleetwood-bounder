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
  
  // one of the next 3 specifies the how the value is determined at runtime
  protected Object value;
  protected VariableDefinitionImpl variableDefinition;
  protected Script expression;

  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;
  public ParameterInstanceImpl parent;

  protected String buildVariableDefinitionRefName;
  protected String buildExpression;
  protected String buildExpressionLanguage;
  protected Long buildLine;
  protected Long buildColumn;

  public ParameterBindingImpl value(Object value) {
    this.value = value;
    return this;
  }
  
  public ParameterBindingImpl expression(String expression) {
    this.buildExpression = expression;
    return this;
  }
  
  public ParameterBindingImpl expressionLanguage(String expressionLanguage) {
    this.buildExpressionLanguage = expressionLanguage;
    return this;
  }
  
  public ParameterBindingImpl variable(String variableDefinitionRefName) {
    this.buildVariableDefinitionRefName = variableDefinitionRefName;
    return this;
  }

  public void parse(ParseContext parseContext) {
    int valueSpecifications = 0;
    ParameterInstanceImpl parameterInstance = parseContext.getContextObject(ParameterInstanceImpl.class);
    ScopeDefinitionImpl scopeDefinition = parseContext.getContextObject(ScopeDefinitionImpl.class);
    ActivityParameter activityParameter = parameterInstance.activityParameter;
    if (value!=null) {
      try {
        value = activityParameter.type.convertApiToInternalValue(value);
      } catch (InvalidApiValueException e) {
        parseContext.addError(buildLine, buildColumn, "Couldn't parse parameter %s binding value %s as a %s: %s", parameterInstance.name, value, activityParameter.type, e.getMessage());
      }
      valueSpecifications++;
    }
    if (buildVariableDefinitionRefName!=null) {
      variableDefinition = scopeDefinition.findVariableDefinitionByName(buildVariableDefinitionRefName);
      if (!variableDefinition.type.equals(activityParameter.type)) {
        parseContext.addError(buildLine, buildColumn, "Variable %s (%s) can't be bound to parameter %s (%s) because the types don't match", 
                buildVariableDefinitionRefName, 
                variableDefinition.type.getId(), 
                parameterInstance.name,
                activityParameter.type.getId());
      }
      valueSpecifications++;
    }
    if (this.expression!=null) {
      String expressionLanguage = buildExpressionLanguage!=null ? buildExpressionLanguage : Scripts.JAVASCRIPT;
      try {
        expression = processEngine.scripts.compile(buildExpression, expressionLanguage);
      } catch (Exception e) {
        parseContext.addError(buildLine, buildColumn, "Couldn't compile %s expression: %s: %s", expressionLanguage, e.getMessage(), this.expression);
      }
      valueSpecifications++;
    }
    if (valueSpecifications==0) {
      parseContext.addWarning(buildLine, buildColumn, "No value, variableDefinitionName or expression specified");
    }
    if (valueSpecifications>1) {
      parseContext.addWarning(buildLine, buildColumn, "More then one value specified");
    }
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


}

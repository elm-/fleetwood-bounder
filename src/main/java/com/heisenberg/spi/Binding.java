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
package com.heisenberg.spi;

import com.heisenberg.expressions.Script;
import com.heisenberg.expressions.ScriptResult;
import com.heisenberg.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class Binding<T> {
  
  public Object value;
  public String variableName;
  public String expression;
  public Script expressionScript;

  @SuppressWarnings("unchecked")
  public T getValue(ControllableActivityInstance activityInstance) {
    if (value!=null) {
      return (T) value;
    }
    if (variableName!=null) {
      return (T) activityInstance.getVariableValueRecursive(variableName).getValue();
    }
    if (expressionScript!=null) {
      ScriptResult result = activityInstance.getScriptRunner().evaluateScript(activityInstance, expressionScript);
      return (T) result.getResult();
    }
    return null;
  }
  
  public Binding<T> value(T value) {
    this.value = value;
    return this;
  }
  
  public Binding<T> variableName(String variableName) {
    this.variableName = variableName;
    return this;
  }
  
  public Binding<T> expression(String expression) {
    this.expression = expression;
    return this;
  }
  
// Old parsing code for binding that might come in handy
// 
//  public void parse(ValidateProcessDefinitionAfterDeserialization validateProcessDefinitionAfterDeserialization) {
//    int valueSpecifications = 0;
//    ParameterInstanceImpl parameterInstance = validateProcessDefinitionAfterDeserialization.getContextObject(ParameterInstanceImpl.class);
//    ScopeDefinitionImpl scopeDefinition = validateProcessDefinitionAfterDeserialization.getContextObject(ScopeDefinitionImpl.class);
//    ActivityParameter activityParameter = parameterInstance.activityParameter;
//    if (value!=null) {
//      try {
//        value = activityParameter.type.convertApiToInternalValue(value);
//      } catch (InvalidApiValueException e) {
//        validateProcessDefinitionAfterDeserialization.addError(buildLine, buildColumn, "Couldn't parse parameter %s binding value %s as a %s: %s", parameterInstance.name, value, activityParameter.type, e.getMessage());
//      }
//      valueSpecifications++;
//    }
//    if (buildVariableDefinitionRefName!=null) {
//      variableDefinition = scopeDefinition.findVariableDefinitionByName(buildVariableDefinitionRefName);
//      if (!variableDefinition.type.equals(activityParameter.type)) {
//        validateProcessDefinitionAfterDeserialization.addError(buildLine, buildColumn, "Variable %s (%s) can't be bound to parameter %s (%s) because the types don't match", 
//                buildVariableDefinitionRefName, 
//                variableDefinition.type.getId(), 
//                parameterInstance.name,
//                activityParameter.type.getId());
//      }
//      valueSpecifications++;
//    }
//    if (this.expression!=null) {
//      String expressionLanguage = buildExpressionLanguage!=null ? buildExpressionLanguage : Scripts.JAVASCRIPT;
//      try {
//        expression = processEngine.scripts.compile(buildExpression, expressionLanguage);
//      } catch (Exception e) {
//        validateProcessDefinitionAfterDeserialization.addError(buildLine, buildColumn, "Couldn't compile %s expression: %s: %s", expressionLanguage, e.getMessage(), this.expression);
//      }
//      valueSpecifications++;
//    }
//    if (valueSpecifications==0) {
//      validateProcessDefinitionAfterDeserialization.addWarning(buildLine, buildColumn, "No value, variableDefinitionName or expression specified");
//    }
//    if (valueSpecifications>1) {
//      validateProcessDefinitionAfterDeserialization.addWarning(buildLine, buildColumn, "More then one value specified");
//    }
//  }

}

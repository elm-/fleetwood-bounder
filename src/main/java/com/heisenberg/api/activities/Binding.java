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
package com.heisenberg.api.activities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.InvalidValueException;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.PluginConfigurationField;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.script.Script;
import com.heisenberg.impl.script.ScriptResult;


/**
 * @author Walter White
 */
public class Binding<T> {
  
  @JsonIgnore
  public ProcessEngineImpl processEngine;
  @JsonIgnore
  public DataType dataType;
  
  public Object value;
  public Object valueJson;
  public Object variableDefinitionId;
  public String expression;
  public String expressionLanguage; // optional. can be null. default is JavaScript
  @JsonIgnore
  public Script expressionScript;
  
  @SuppressWarnings("unchecked")
  public T getValue(ControllableActivityInstance activityInstance) {
    if (this.processEngine==null) {
      String typeName = ((ActivityInstanceImpl)activityInstance).activityDefinition.activityType.getClass().getName();
      throw new RuntimeException("Please ensure that in the "+typeName+".validate, you call activityDefinition.initializeBindings(Validator)");
    }
    if (this.value!=null) {
      return (T) this.value;
    }
    if (this.variableDefinitionId!=null) {
      return (T) activityInstance.getVariableValueRecursive(this.variableDefinitionId).getValue();
    }
    if (this.expressionScript!=null) {
      ScriptResult scriptResult = activityInstance.getScriptService().evaluateScript(activityInstance, this.expressionScript);
      Object result = scriptResult.getResult();
      return (T) this.dataType.convertScriptValueToInternal(result, this.expressionScript.language);
    }
    return null;
  }

  public Binding<T> value(T value) {
    this.value = value;
    return this;
  }
  
  public Binding<T> valueJson(Object valueJson) {
    this.valueJson = valueJson;
    return this;
  }
  
  public Binding<T> variableDefinitionId(Object variableDefinitionId) {
    this.variableDefinitionId = variableDefinitionId;
    return this;
  }
  
  public Binding<T> expression(String expression) {
    this.expression = expression;
    return this;
  }

  // processEngine and dataType are already initialized when this is called
  public void validate(ActivityDefinitionImpl activityDefinition, ActivityType activityType, PluginConfigurationField descriptorField, Validator validator) {
    if (value!=null) {
      try {
        dataType.validateInternalValue(value);
      } catch (InvalidValueException e) {
        validator.addError("Invalid value '%s' for %s.%s", value, activityType.getClass().getName(), descriptorField.field.getName());
      }
    } else if (valueJson!=null) {
      try {
        value = dataType.convertJsonToInternalValue(valueJson);
      } catch (InvalidValueException e) {
        validator.addError("Invalid json value '%s' for %s.%s", valueJson, activityType.getClass().getName(), descriptorField.field.getName());
      }
    } else if (expression!=null) {
      try {
        expressionScript = processEngine.getScriptService().compile(expression, expressionLanguage);
      } catch (RuntimeException e) {
        Throwable cause = e.getCause();
        if (cause==null) {
          cause = e;
        }
        validator.addError("Invalid expression '%s' for %s.%s: %s", valueJson, activityType.getClass().getName(), descriptorField.field.getName(), e.getMessage());
      }
    }
  }
}

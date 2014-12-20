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
package com.heisenberg.impl.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.impl.script.Script;
import com.heisenberg.impl.script.ScriptService;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.type.InvalidValueException;


/**
 * @author Walter White
 */
public class Binding<T> {
  
  @JsonIgnore
  public boolean isInitialized;
  @JsonIgnore
  public DataType dataType;
  
  public Object value;
  public Object valueJson;
  public String variableDefinitionId;
  public String expression;
  public String expressionLanguage; // optional. can be null. default is JavaScript
  @JsonIgnore
  public Script expressionScript;
  
  public Binding<T> value(T value) {
    this.value = value;
    return this;
  }
  
  public Binding<T> valueJson(Object valueJson) {
    this.valueJson = valueJson;
    return this;
  }
  
  public Binding<T> variableDefinitionId(String variableDefinitionId) {
    this.variableDefinitionId = variableDefinitionId;
    return this;
  }
  
  public Binding<T> expression(String expression) {
    this.expression = expression;
    return this;
  }

  // processEngine and dataType are already initialized when this is called
  public void validate(Activity activity, Validator validator, String bindingFieldName) {
    isInitialized = true;
    if (value!=null) {
      try {
        dataType.validateInternalValue(value);
      } catch (InvalidValueException e) {
        validator.addError("Invalid value '%s' for %s", value, bindingFieldName);
      }
    } else if (valueJson!=null) {
      try {
        value = dataType.convertJsonToInternalValue(valueJson);
      } catch (InvalidValueException e) {
        validator.addError("Invalid json value '%s' for %s", valueJson, bindingFieldName);
      }
    } else if (expression!=null) {
      try {
        ScriptService scriptService = validator.getServiceRegistry().getService(ScriptService.class);
        expressionScript = scriptService.compile(expression, expressionLanguage);
      } catch (RuntimeException e) {
        Throwable cause = e.getCause();
        if (cause==null) {
          cause = e;
        }
        validator.addError("Invalid expression '%s' for %s: %s", valueJson, bindingFieldName, e.getMessage());
      }
    }
  }
}

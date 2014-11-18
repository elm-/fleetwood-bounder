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
  public <V> V getValue(ActivityInstanceImpl activityInstance, Class<V> valueClass) {
    if (value!=null) {
      return (V) value;
    }
    if (variableName!=null) {
      return (V) activityInstance.getVariableValueRecursive(variableName).getValue();
    }
    if (expressionScript!=null) {
      ScriptResult result = activityInstance.processEngine.scripts.evaluateScript(activityInstance, expressionScript);
      return (V) result.getResult();
    }
    return null;
  }
}

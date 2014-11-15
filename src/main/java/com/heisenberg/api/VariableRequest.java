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
package com.heisenberg.api;

import java.util.HashMap;
import java.util.Map;


/**
 * @author Walter White
 */
public class VariableRequest {

  public Map<String,Object> variableValues;

  /** extra user defined information to be stored with the process instance. */
  public Map<String,Object> persistentContext;
  
  /** extra user defined information only accessible in the process as long as this request is executed synchronous. */
  public Map<String,Object> transientContext;

  public VariableRequest variableValue(String variableDefinitionId, Object value) {
    if (variableValues==null) {
      variableValues = new HashMap<>();
    }
    variableValues.put(variableDefinitionId, value);
    return this;
  }

  public VariableRequest persistentContext(String key, Object value) {
    if (persistentContext==null) {
      persistentContext = new HashMap<>();
    }
    persistentContext.put(key, value);
    return this;
  }

  public VariableRequest transientContext(String key, Object value) {
    if (transientContext==null) {
      transientContext = new HashMap<>();
    }
    transientContext.put(key, value);
    return this;
  }
}

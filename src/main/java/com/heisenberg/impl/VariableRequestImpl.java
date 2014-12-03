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
package com.heisenberg.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.impl.definition.VariableDefinitionImpl;


/**
 * @author Walter White
 */
public class VariableRequestImpl {

  @JsonIgnore
  ProcessEngineImpl processEngine;
  
  @JsonIgnore
  public Map<String,Object> variableValues;

  @JsonProperty("variableValues")
  public Map<String,Object> variableValuesJson;

  /** extra user defined information only accessible in the process as long as this request is executed synchronous. */
  public Map<String,Object> transientContext;

//  public VariableRequestImpl() {
//  }

  public VariableRequestImpl(ProcessEngineImpl processEngine) {
    super();
    this.processEngine = processEngine;
  }

  public VariableRequestImpl variableValue(String variableDefinitionId, Object value) {
    if (variableValues==null) {
      variableValues = new LinkedHashMap<>();
    }
    variableValues.put(variableDefinitionId, value);
    return this;
  }

  public VariableRequestImpl variableValueJson(String variableDefinitionId, Object valueJson) {
    if (variableValuesJson==null) {
      variableValuesJson = new LinkedHashMap<>();
    }
    variableValuesJson.put(variableDefinitionId, valueJson);
    return this;
  }
  
  public VariableRequestImpl transientContext(String key, Object value) {
    if (transientContext==null) {
      transientContext = new HashMap<>();
    }
    transientContext.put(key, value);
    return this;
  }
}

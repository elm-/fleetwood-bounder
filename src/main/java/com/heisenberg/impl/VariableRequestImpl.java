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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.definition.VariableImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.type.JavaBeanType;


/**
 * @author Walter White
 */
public abstract class VariableRequestImpl {

  @JsonIgnore
  public JsonService jsonService;
  
  public Map<String,Object> variableValues;
  public Map<String,DataType> variableDataTypes;

  /** extra user defined information only accessible in the process as long as this request is executed synchronous. */
  public Map<String,Object> transientContext;

  public VariableRequestImpl() {
  }

  public VariableRequestImpl(JsonService jsonService) {
    super();
    this.jsonService = jsonService;
  }
  
  public void deserialize(WorkflowImpl processDefinition) {
    if (variableValues!=null && !variableValues.isEmpty()) {
      Map<String,Object> internalValues = new HashMap<>();
      List<VariableImpl> variableDefinitions = processDefinition.getVariableDefinitions();
      if (variableDefinitions!=null) {
        for (VariableImpl variableDefinition: variableDefinitions) {
          String variableId = variableDefinition.id;
          Object jsonValue = variableValues.get(variableId);
          Object internalValue = variableDefinition.dataType.convertJsonToInternalValue(jsonValue);
          internalValues.put(variableId, internalValue);
        }
      }
      variableValues = internalValues;
    }
  }

  public VariableRequestImpl variableValue(String variableDefinitionId, Object value) {
    if (variableValues==null) {
      variableValues = new LinkedHashMap<>();
    }
    variableValues.put(variableDefinitionId, value);
    return this;
  }

  public VariableRequestImpl variableValue(String variableDefinitionId, Object value, DataType dataType) {
    if (variableValues==null) {
      variableValues = new LinkedHashMap<>();
      variableDataTypes = new HashMap<>();
    }
    variableValues.put(variableDefinitionId, value);
    variableDataTypes.put(variableDefinitionId, dataType);
    return this;
  }

  public VariableRequestImpl variableValue(String variableDefinitionId, Object value, Class<?> javaBeanClass) {
    JavaBeanType javaBeanType = new JavaBeanType(javaBeanClass);
    javaBeanType.setJsonService(jsonService);
    variableValue(variableDefinitionId, value, javaBeanType);
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

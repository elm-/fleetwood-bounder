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
package com.heisenberg.api.builder;

import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.type.DataType;

/**
 * @author Walter White
 */
public interface StartBuilder {

  StartBuilder processDefinitionId(String processDefinitionId);

  StartBuilder processDefinitionName(String processDefinitionName);

  StartBuilder variableValue(String variableDefinitionId, Object internalValue);

  /** Set the variable and provide the dataType, the dataType is required when using the ClientProcessEngine! */
  StartBuilder variableValue(String variableDefinitionId, Object internalValue, DataType dataType);
  /** Set the variable and provide the dataType as a java bean type, the dataType is required when using the ClientProcessEngine! */
  StartBuilder variableValue(String variableDefinitionId, Object value, Class<?> javaBeanClass);
  
  StartBuilder transientContext(String key, Object value);

  WorkflowInstance startProcessInstance();

}
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

import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.type.DataType;

/**
 * @author Walter White
 */
public interface TriggerBuilder {

  TriggerBuilder processDefinitionId(String processDefinitionId);

  TriggerBuilder variableValue(String variableDefinitionId, Object internalValue);

  /** converts the internal value to json and sets that as the value in the trigger message so that the message is serializable */
  TriggerBuilder variableValue(String variableDefinitionId, Object internalValue, DataType dataType);

  TriggerBuilder transientContext(String key, Object value);

  ProcessInstance startProcessInstance();

}
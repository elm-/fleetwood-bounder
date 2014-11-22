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
package com.heisenberg.api.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
public abstract class AbstractDataType implements DataType {

  /** gets injected automatically */
  @JsonIgnore
  public ProcessEngine processEngine;

  @Override
  public void validate(Validator validator) {
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return internalValue;
  }

  @Override
  public Object convertInternalToScriptValue(Object internalValue, String language) {
    return internalValue;
  }

  @Override
  public Object convertScriptValueToInternal(Object scriptValue, String language) {
    return scriptValue;
  }
}

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

import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
public class SingletonDataTypeProxy implements DataType {
  
  String dataTypeId;
  
  @Override
  public String getType() {
    return dataTypeId;
  }

  @Override
  public String getLabel() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return null;
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return null;
  }

  @Override
  public Object convertInternalToScriptValue(Object internalValue, String language) {
    return null;
  }

  @Override
  public Object convertScriptValueToInternal(Object scriptValue, String language) {
    return null;
  }

  @Override
  public void validate(Validator validator) {
  }
}

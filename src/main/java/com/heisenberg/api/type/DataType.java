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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.heisenberg.api.util.Plugin;
import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public interface DataType extends Plugin {
  
  /** invoked to validate values submitted through the api. */
  void validateInternalValue(Object internalValue) throws InvalidValueException;

  /** only returns valid internal values. */
  Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException;

  Object convertInternalToJsonValue(Object internalValue);

  Object convertInternalToScriptValue(Object internalValue, String language);

  Object convertScriptValueToInternal(Object scriptValue, String language);

  void validate(Validator validator);
}

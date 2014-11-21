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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.heisenberg.api.type.TextType;
import com.heisenberg.type.ProcessDefinitionIdType;


/**
 * @author Walter White
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public interface DataType extends Spi {
  
  TextType TEXT = new TextType();
  DataType PROCESS_DEFINITION_ID = new ProcessDefinitionIdType();
  
  String getId();

  Object convertJsonToInternalValue(Object apiValue) throws InvalidApiValueException;

  Object convertInternalToJsonValue(Object internalValue);

  Object convertInternalToScriptValue(Object internalValue, String language);

  Object convertScriptValueToInternal(Object scriptValue, String language);

  void validate(Validator validator);
}

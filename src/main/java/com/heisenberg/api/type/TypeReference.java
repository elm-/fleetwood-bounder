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
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
@JsonTypeName("typeReference")
public class TypeReference implements DataType {

  public String typeId;
  
  @JsonIgnore
  public DataType delegate;
  
  public TypeReference() {
  }

  public TypeReference(String typeId) {
    this.typeId = typeId;
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return delegate.convertJsonToInternalValue(jsonValue);
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    delegate.validateInternalValue(internalValue);
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    return delegate.convertInternalToJsonValue(internalValue);
  }

  @Override
  public Object convertInternalToScriptValue(Object internalValue, String language) {
    return delegate.convertInternalToScriptValue(internalValue, language);
  }

  @Override
  public Object convertScriptValueToInternal(Object scriptValue, String language) {
    return delegate.convertScriptValueToInternal(scriptValue, language);
  }

  @Override
  public void validate(Validator validator) {
    delegate.validate(validator);
  }

}

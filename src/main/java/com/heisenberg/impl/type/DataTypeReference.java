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
package com.heisenberg.impl.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
@JsonTypeName("dataTypeReference")
public class DataTypeReference implements DataType {

  public String dataTypeId;
  
  @JsonIgnore
  public DataType delegate;
  
  public DataTypeReference() {
  }

  public DataTypeReference(String dataTypeId) {
    this.dataTypeId = dataTypeId;
  }

  public DataTypeReference(String dataTypeId, DataType delegate) {
    Exceptions.checkNotNullParameter(dataTypeId, "dataTypeId");
    this.dataTypeId = dataTypeId;
    this.delegate = delegate; // potentially, with a client process engine, the delegate could be null
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    return delegate.convertJsonToInternalValue(jsonValue);
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if (delegate!=null) {
      delegate.validateInternalValue(internalValue);
    }
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
    if (delegate==null) {
      if (dataTypeId!=null) {
        delegate = validator.getDataTypes().findByTypeId(dataTypeId);
        if (delegate==null) {
          validator.addError("Invalid dataTypeId specified: "+dataTypeId);
        }
      } else {
        validator.addError("No typeId specified");
      }
    }
    if (delegate!=null) {
      delegate.validate(validator);
    }
  }

}

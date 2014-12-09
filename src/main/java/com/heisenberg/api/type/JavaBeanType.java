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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
@JsonTypeName("javaBean")
public class JavaBeanType extends AbstractDataType {
  
  public Class<?> javaClass;
  public JsonService jsonService;

  public JavaBeanType() {
  }

  public JavaBeanType(Class< ? > javaClass) {
    this.javaClass = javaClass;
  }

  public JavaBeanType(Class< ? > javaClass, JsonService jsonService) {
    this.javaClass = javaClass;
    this.jsonService = jsonService;
  }

  @Override
  public void validate(Validator validator) {
    this.jsonService = validator.getJsonService();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    if (jsonValue==null) return null;
    if (Map.class.isAssignableFrom(jsonValue.getClass())) {
      return jsonService.jsonMapToObject((Map<String,Object>)jsonValue, javaClass);
    }
    throw new InvalidValueException("Couldn't convert json: "+jsonValue+" ("+jsonValue.getClass().getName()+")");
  }
  
  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    if (internalValue==null) return null;
    return jsonService.objectToJsonMap(internalValue);
  }
  
  public JsonService getJsonService() {
    return jsonService;
  }
  
  public void setJsonService(JsonService jsonService) {
    this.jsonService = jsonService;
  }
}

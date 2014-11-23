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

import java.util.Map;

import com.heisenberg.api.type.AbstractDataType;
import com.heisenberg.api.type.InvalidValueException;


/**
 * @author Walter White
 */
public class JavaBeanType extends AbstractDataType {
  
  Class<?> javaClass;
  
  public JavaBeanType(Class< ? > javaClass) {
    this.javaClass = javaClass;
  }

  @Override
  public String getId() {
    return javaClass.getName();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    if (jsonValue==null) return null;
    if (Map.class.isAssignableFrom(jsonValue.getClass())) {
      return ((ProcessEngineImpl)processEngine).json.jsonMapToObject((Map<String,Object>)jsonValue, javaClass);
    }
    throw new RuntimeException("Couldn't convert json: "+jsonValue+" ("+jsonValue.getClass().getName()+")");
  }

  @Override
  public String getLabel() {
    return javaClass.getSimpleName();
  }
}
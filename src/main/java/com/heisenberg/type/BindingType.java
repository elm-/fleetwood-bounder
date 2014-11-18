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
package com.heisenberg.type;

import java.lang.reflect.TypeVariable;

import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class BindingType extends Type {
  
  String id;
  String label;

  public BindingType(java.lang.reflect.Type targetType) {
    Class< ? > targetJavaType = (Class<?>)targetType;
    // TODO get the id from some flyweight
    try {
      String targetTypeId = ((Type)targetJavaType.newInstance()).getId();
      id = "binding<"+targetTypeId+">";
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Object convertApiToInternalValue(Object apiValue) throws InvalidApiValueException {
    return apiValue;
  }

}

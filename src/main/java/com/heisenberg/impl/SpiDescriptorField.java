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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.type.ListType;
import com.heisenberg.spi.ConfigurationField;
import com.heisenberg.spi.DataType;
import com.heisenberg.type.BindingType;


/**
 * @author Walter White
 */
public class SpiDescriptorField {

  public String name;
  public String label;
  public DataType dataType;
  
  public SpiDescriptorField(ProcessEngineImpl processEngine, Field javaField) {
    this.name = javaField.getName();
    ConfigurationField configurationField = javaField.getAnnotation(ConfigurationField.class);
    this.label = configurationField!=null ? configurationField.value() : null;
    this.dataType = getDataType(javaField, processEngine);
  }

  public static DataType getDataType(Field javaField, ProcessEngineImpl processEngine) {
    return getDataType(javaField.getGenericType(), processEngine);
  }

  public static DataType getDataType(java.lang.reflect.Type genericType, ProcessEngineImpl processEngine) {
    if (String.class == genericType) {
      return DataType.TEXT;
    } else if (genericType instanceof ParameterizedType) {
      ParameterizedType parametrizedType = (ParameterizedType) genericType;
      java.lang.reflect.Type[] typeArgs = parametrizedType.getActualTypeArguments();
      java.lang.reflect.Type rawType = parametrizedType.getRawType();
      if (Binding.class==rawType) {
        return new BindingType(getDataType(typeArgs[0], processEngine));
      } else if (List.class==rawType) {
        return new ListType(getDataType(typeArgs[0], processEngine));
      } 
    } else if (genericType instanceof Class){
      Class<?> clazz = (Class< ? >) genericType;
      DataType dataType = processEngine.dataTypes.get(clazz.getName());
      if (dataType!=null) {
        return dataType;
      }
    }
    return null;
  }
}

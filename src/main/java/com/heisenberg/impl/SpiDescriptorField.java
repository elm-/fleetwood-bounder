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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.spi.Binding;
import com.heisenberg.spi.ConfigurationField;
import com.heisenberg.spi.DataType;


/**
 * @author Walter White
 */
public class SpiDescriptorField {

  @JsonIgnore 
  ProcessEngineImpl processEngine;
  
  public String name;
  public String label;
  public String typeId;
  
  public SpiDescriptorField(ProcessEngineImpl processEngine, Field javaField) {
    this.processEngine = processEngine;
    this.name = javaField.getName();
    ConfigurationField configurationField = javaField.getAnnotation(ConfigurationField.class);
    this.label = configurationField!=null ? configurationField.value() : null;
    typeId = getTypeId(javaField.getGenericType());
  }

  private String getTypeId(java.lang.reflect.Type type) {
    if (String.class == type) {
      return DataType.TEXT.getId();
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parametrizedType = (ParameterizedType) type;
      java.lang.reflect.Type[] typeArgs = parametrizedType.getActualTypeArguments();
      java.lang.reflect.Type rawType = parametrizedType.getRawType();
      if (Binding.class==rawType) {
        return "binding<"+getTypeId(typeArgs[0])+">";
      } else if (List.class==rawType) {
        return "list<"+getTypeId(typeArgs[0])+">";
      } 
    } else if (type instanceof Class){
      Class<?> clazz = (Class< ? >) type;
      DataType dataType = processEngine.dataTypes.get(clazz.getName());
      if (dataType!=null) {
        return dataType.getId();
      }
    }
    return null;
  }
}

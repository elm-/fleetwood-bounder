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
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.ListType;
import com.heisenberg.api.type.TextType;


/**
 * @author Walter White
 */
public class SpiDescriptorField {

  public String name;
  public String label;
  public boolean isRequired;
  
  @JsonIgnore
  public DataType dataType;
  public String dataTypeId;
  @JsonProperty("dataType")
  public Map<String,Object> dataTypeJson;
  
  @JsonIgnore
  public Field field;
  
  public SpiDescriptorField(ProcessEngineImpl processEngine, Field field, ConfigurationField configurationField) {
    this.name = field.getName();
    this.field = field;
    this.field.setAccessible(true);
    this.dataType = getDataType(field, processEngine);
    if (this.dataType.getId()!=null) {
      dataTypeId = this.dataType.getId(); 
    } else {
      dataTypeJson = processEngine.json.objectToJsonMap(dataType);
    }
    if (configurationField!=null) {
      this.label = configurationField.value();
      this.isRequired = configurationField.required();
    } else {
      this.label = name;
    }
  }

  public static DataType getDataType(Field field, ProcessEngineImpl processEngine) {
    return getDataType(field, field.getGenericType(), processEngine);
  }

  public static DataType getDataType(Field field, java.lang.reflect.Type genericType, ProcessEngineImpl processEngine) {
    if (String.class == genericType) {
      return TextType.INSTANCE;
    } else if (genericType instanceof ParameterizedType) {
      ParameterizedType parametrizedType = (ParameterizedType) genericType;
      java.lang.reflect.Type[] typeArgs = parametrizedType.getActualTypeArguments();
      java.lang.reflect.Type rawType = parametrizedType.getRawType();
      if (Binding.class==rawType) {
        return new BindingType(getDataType(field, typeArgs[0], processEngine));
      } else if (List.class==rawType) {
        return new ListType(getDataType(field, typeArgs[0], processEngine));
      } 
    } else if (genericType instanceof Class){
      Class<?> clazz = (Class< ? >) genericType;
      DataType dataType = processEngine.dataTypes.get(clazz.getName());
      if (dataType==null) {
        // auto register java bean types that are used as configurations inside activity types.
        processEngine.registerJavaBeanType(clazz);
        dataType = processEngine.dataTypes.get(clazz.getName());
      }
      if (dataType!=null) {
        return dataType;
      }
    }
    throw new RuntimeException("Don't know how to handle "+genericType+"'s.  It's used in configuration field: "+field);
  }
}

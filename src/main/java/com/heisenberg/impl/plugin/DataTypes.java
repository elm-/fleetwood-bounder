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
package com.heisenberg.impl.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.type.BindingType;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.JavaBeanType;
import com.heisenberg.api.type.ListType;
import com.heisenberg.api.type.TypeReference;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public class DataTypes {
  
  public List<TypeDescriptor> descriptors = new ArrayList<>();
  public Map<String,DataType> dataTypesById = new HashMap<>();
  public Map<Type,TypeDescriptor> descriptorsByType = new HashMap<>();

  /** creates a descriptor for a java bean type */
  public TypeDescriptor registerJavaBeanType(Class<?> javaBeanClass) {
    Exceptions.checkNotNullParameter(javaBeanClass, "javaBeanClass");
    return registerSingletonDataType(new JavaBeanType(javaBeanClass), javaBeanClass.getName(), javaBeanClass); 
  }

  /** creates a descriptor for a configurable dataType */
  public TypeDescriptor registerSingletonDataType(DataType dataType) {
    Exceptions.checkNotNullParameter(dataType, "dataTypeDescriptor.dataType");
    String typeId = getJsonTypeName(dataType);
    addDataTypeById(typeId, dataType);
    TypeDescriptor dataTypeDescriptor = new TypeDescriptor(new TypeReference(typeId));
    descriptors.add(dataTypeDescriptor);
    return dataTypeDescriptor;
  }

  protected String getJsonTypeName(DataType dataType) {
    Class< ? extends DataType> dataTypeClass = dataType.getClass();
    JsonTypeName jsonTypeName = dataTypeClass.getAnnotation(JsonTypeName.class);
    if (jsonTypeName==null) {
      throw new RuntimeException("Activity type "+dataTypeClass+" doesn't have JsonTypeName annotation");
    }
    return jsonTypeName.value();
  }

  public TypeDescriptor registerSingletonDataType(DataType dataType, Class<?> valueClass) {
    return registerSingletonDataType(dataType, getJsonTypeName(dataType), valueClass);
  }

  /** @param valueClass is used when scanning configurations: this dataType will be used for 
   * all configuration fields of this valueClass */
  public TypeDescriptor registerSingletonDataType(DataType dataType, String typeId, Class<?> valueClass) {
    Exceptions.checkNotNullParameter(dataType, "dataType");
    TypeDescriptor descriptor = registerSingletonDataType(dataType, typeId);
    descriptorsByType.put(valueClass, descriptor);
    descriptors.add(descriptor);
    return descriptor;
  }
    
  /** creates a descriptor for a configurable dataType */
  public TypeDescriptor registerSingletonDataType(DataType dataType, String typeId) {
    addDataTypeById(typeId, dataType);
    TypeDescriptor dataTypeDescriptor = new TypeDescriptor(new TypeReference(typeId));
    descriptors.add(dataTypeDescriptor);
    return dataTypeDescriptor;
  }

  protected void addDataTypeById(String typeId, DataType dataType) {
    if (dataTypesById.containsKey(typeId)) {
      throw new RuntimeException("Duplicate type declaration for id "+typeId);
    }
    dataTypesById.put(typeId, dataType);
  }
  
  public TypeDescriptor registerConfigurableDataType(DataType dataType) {
    TypeDescriptor robert = new TypeDescriptor(dataType);
    robert.analyze(this); // :)
    descriptors.add(robert);
    return robert;
  }


  
  public TypeDescriptor getTypeDescriptor(Field field) {
    return getTypeDescriptor(field.getGenericType(), field);
  }

  protected TypeDescriptor getTypeDescriptor(Type type, Field field /* passed for error message only */) {
    TypeDescriptor descriptor = descriptorsByType.get(type);
    if (descriptor!=null) {
      return descriptor;
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parametrizedType = (ParameterizedType) type;
      Type rawType = parametrizedType.getRawType();
      Type[] typeArgs = parametrizedType.getActualTypeArguments();

      descriptor = createDescriptor(rawType, typeArgs, field);
      descriptorsByType.put(type, descriptor);
      return descriptor;
    }
    throw new RuntimeException("Don't know how to handle "+type+"'s.  It's used in configuration field: "+field);
  }

  private TypeDescriptor createDescriptor(Type rawType, Type[] typeArgs, Field field /* passed for error message only */) {
    if (Binding.class==rawType) {
      TypeDescriptor argDescriptor = getTypeDescriptor(typeArgs[0], field);
      BindingType bindingType = new BindingType(argDescriptor.dataType);
      return new TypeDescriptor(bindingType);
    } else if (List.class==rawType) {
      TypeDescriptor argDescriptor = getTypeDescriptor(typeArgs[0], field);
      ListType listType = new ListType(argDescriptor.dataType);
      return new TypeDescriptor(listType);
    } 
    throw new RuntimeException("Don't know how to handle generic type "+rawType+"'s.  It's used in configuration field: "+field);
  }

}

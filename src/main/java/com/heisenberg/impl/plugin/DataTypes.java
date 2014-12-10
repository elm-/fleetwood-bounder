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

import static com.heisenberg.impl.plugin.PluginHelper.couldBeConfigured;
import static com.heisenberg.impl.plugin.PluginHelper.getJsonTypeName;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.impl.type.BindingType;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.type.DataTypeReference;
import com.heisenberg.impl.type.JavaBeanType;
import com.heisenberg.impl.type.ListType;
import com.heisenberg.impl.type.TextType;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public class DataTypes {
  
  public List<TypeDescriptor> descriptors = new ArrayList<>();
  public Map<String,DataType> dataTypesById = new HashMap<>();
  public Map<Type,TypeDescriptor> descriptorsByType = new HashMap<>();
  public ObjectMapper objectMapper;
  public JsonService jsonService;
  
  public DataTypes(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
  
  public ListType newListType(DataType dataType) {
    return new ListType(dataType);
  }

  public DataTypeReference newJavaBeanType(Class< ? > javaBeanType) {
    return new DataTypeReference(javaBeanType.getName(), new JavaBeanType(javaBeanType, jsonService));
  }

  public void registerDefaultDataTypes() {
    registerSingletonDataType(new TextType(), String.class);
  }

  public TypeDescriptor registerConfigurableDataType(DataType dataType) {
    TypeDescriptor robertDn = new TypeDescriptor(dataType);
    robertDn.analyze(this); // :)
    addDescriptor(robertDn);
    return robertDn;
  }

  /** creates a descriptor for a java bean type */
  public TypeDescriptor registerJavaBeanType(Class<?> javaBeanClass) {
    Exceptions.checkNotNullParameter(javaBeanClass, "javaBeanClass");
    return registerSingletonDataType(new JavaBeanType(javaBeanClass), javaBeanClass.getName(), javaBeanClass); 
  }

  /** creates a descriptor for a configurable dataType */
  public TypeDescriptor registerSingletonDataType(DataType dataType) {
    return registerSingletonDataType(dataType, getJsonTypeName(dataType), null);
  }

  /** creates a descriptor for a configurable dataType */
  public TypeDescriptor registerSingletonDataType(DataType dataType, String typeId) {
    return registerSingletonDataType(dataType, typeId, null);
  }

  public TypeDescriptor registerSingletonDataType(DataType dataType, Class<?> valueClass) {
    return registerSingletonDataType(dataType, getJsonTypeName(dataType), valueClass);
  }

  /** @param valueClass is used when scanning configurations: this dataType will be used for 
   * all configuration fields of this valueClass */
  public TypeDescriptor registerSingletonDataType(DataType dataType, String typeId, Class<?> valueClass) {
    Exceptions.checkNotNullParameter(dataType, "dataType");
    Exceptions.checkNotNull(typeId, "Activity type "+dataType.getClass()+" doesn't have JsonTypeName annotation");

    addDataTypeById(typeId, dataType);
    if (couldBeConfigured(dataType)) {
      // we need to keep track of the singleton object and reference it 
      dataType = new DataTypeReference(typeId);
    } // else we can just let json use the default constructor 

    TypeDescriptor descriptor = new TypeDescriptor(dataType);
    addDescriptor(descriptor);

    if (valueClass!=null) {
      descriptorsByType.put(valueClass, descriptor);
    }
    
    return descriptor;
  }

  protected void addDescriptor(TypeDescriptor descriptor) {
    descriptors.add(descriptor);
    objectMapper.registerSubtypes(descriptor.dataType.getClass());
  }


  protected void addDataTypeById(String typeId, DataType dataType) {
    if (dataTypesById.containsKey(typeId)) {
      throw new RuntimeException("Duplicate type declaration for id "+typeId);
    }
    dataTypesById.put(typeId, dataType);
  }
  
  protected TypeDescriptor getTypeDescriptor(Field field) {
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

  protected TypeDescriptor createDescriptor(Type rawType, Type[] typeArgs, Field field /* passed for error message only */) {
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
  
  public DataType findByTypeId(String typeId) {
    return dataTypesById.get(typeId);
  }

  public DataType createDataTypeReference(String dataTypeId) {
    DataType delegate = dataTypesById.get(dataTypeId);
    return new DataTypeReference(dataTypeId, delegate);
  }

}

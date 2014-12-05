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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.type.BindingType;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.JavaBeanType;
import com.heisenberg.api.type.ListType;
import com.heisenberg.api.type.TypeReference;
import com.heisenberg.impl.util.Reflection;


/**
 * @author Walter White
 */
public class DataTypes {
  
  public List<DataTypeDescriptor> descriptorsByType = new ArrayList<>();
  public Map<String,DataType> dataTypesById = new LinkedHashMap<>();

  /** creates a descriptor for a java bean type */
  public TypeDescriptor registerJavaBeanType(Class<?> javaBeanClass) {
    return registerDataType(new JavaBeanType(javaBeanClass), javaBeanClass.getName()); 
  }

  public DataTypeDescriptor registerDataType(DataType dataType) {
    return registerDataType(dataType, null);
  }

  /** creates a descriptor for a configurable dataType */
  public DataTypeDescriptor registerDataType(DataType dataType, String typeId) {
    addDataTypeById(typeId, dataType);
    DataTypeDescriptor dataTypeDescriptor = new DataTypeDescriptor(new TypeReference(typeId));
    descriptorsByType.add(dataTypeDescriptor);
    return dataTypeDescriptor;
  }
  
  protected void addDataTypeById(String typeId, DataType dataType) {
    if (dataTypesById.containsKey(typeId)) {
      throw new RuntimeException("Duplicate type declaration for id "+typeId);
    }
    dataTypesById.put(typeId, dataType);
  }
  
  /** creates a descriptor for a configurable dataType */
  public DataTypeDescriptor registerDataType(Class<? extends DataType> dataTypeClass) {
    DataTypeDescriptor robert = new DataTypeDescriptor(dataTypeClass, this);
    robert.analyze(this); // :)
    
    addDataTypeDescriptor(dataTypeDescriptor);
    return dataTypeDescriptor;
  }
  
  protected void addDataTypeDescriptor(DataTypeDescriptor dataTypeDescriptor) {
    if (dataTypeDescriptor==null) {
      throw new RuntimeException("dataTypeDescriptor is null");
    }
    if (descriptorsByType.containsKey(id)) {
      throw new RuntimeException("Duplicate data type descriptor: "+id);
    }
    DataType dataType = dataTypeDescriptor.dataType;
    Class< ? extends DataType> dataTypeClass = dataTypeDescriptor.dataTypeClass;
    if (dataType!=null && dataTypeDescriptor.dataTypeClass!=null) {
      throw new RuntimeException("Data type descriptor should specify a class or an object, but not both: "+id);
    }
    if (dataType==null && dataTypeClass==null) {
      throw new RuntimeException("Data type descriptor doesn't specify a class or an object: "+id);
    }
    descriptorsByType.put(id, dataTypeDescriptor);
    typesByClass.put(dataTypeDescriptor.dataTypeClass, id);
  }
  
//  public String getType(Class< ? > clazz) {
//    return typesByClass.get(clazz);
//  }
//
//  public DataType getDataType(String type) {
//    DataTypeDescriptor descriptor = descriptorsByType.get(type);
//    return descriptor!=null ? descriptor.dataType : null;
//  }
//
//  public void setDataTypeForClass(Class< ? > valueClass, DataType dataType) {
//    dataTypesByValueClass.put(valueClass, dataType);
//  }

  public TypeDescriptor getTypeDescriptor(Field field) {
    return getTypeDescriptor(field, field.getGenericType());
  }

  public TypeDescriptor getTypeDescriptor(Field field, java.lang.reflect.Type genericType) {
    TypeDescriptor typeDescriptor = descriptorsByType.get(genericType);
    if (typeDescriptor != null) {
      return typeDescriptor;
    }
    if (genericType instanceof ParameterizedType) {
      ParameterizedType parametrizedType = (ParameterizedType) genericType;
      java.lang.reflect.Type[] typeArgs = parametrizedType.getActualTypeArguments();
      java.lang.reflect.Type rawType = parametrizedType.getRawType();
      if (Binding.class==rawType) {
        return createBindingDescriptor(getTypeDescriptor(field, typeArgs[0]));
      } else if (List.class==rawType) {
        return createListDescriptor(getTypeDescriptor(field, typeArgs[0]));
      } 
    } else if (genericType instanceof Class){
      Class<?> clazz = (Class< ? >) genericType;
      typeDescriptor = getTypeDescriptorByValueClass(clazz, true);
      if (typeDescriptor!=null) {
        return typeDescriptor;
      }
    }
    throw new RuntimeException("Don't know how to handle "+genericType+"'s.  It's used in configuration field: "+field);
  }

  private TypeDescriptor createBindingDescriptor(TypeDescriptor typeDescriptor) {
    TypeDescriptor bindingDescriptor = new TypeDescriptor();
    bindingDescriptor.id
    return null;
  }

//  private TypeDescriptor getTypeDescriptorByValueClass(Class< ? > clazz, boolean autoCreate) {
//    DataType dataType = dataTypesByValueClass.get(clazz);
//    if (dataType!=null) {
//      return dataType;
//    }
//    if (autoCreate) {
//      DataTypeDescriptor javaBeanDescriptor = registerJavaBeanType(clazz);
//      return javaBeanDescriptor.dataType;
//    }
//    return null;
//  }
//
//  protected DataType getDataType(DataTypeDescriptor dataTypeDescriptor) {
//    if (dataTypeDescriptor.dataType!=null) {
//      return dataTypeDescriptor.dataType;
//    }
//    try {
//      return dataTypeDescriptor.dataTypeClass.newInstance();
//    } catch (Exception e) {
//      throw new RuntimeException("Couldn't instantiate new data type "+dataTypeDescriptor.dataTypeClass+": "+e.getMessage(),e);
//    }
//  }
//
//  public Class< ? > getDataTypeClazz(String typeId) {
//    DataTypeDescriptor dataTypeDescriptor = descriptorsByType.get(typeId);
//    if (dataTypeDescriptor==null) {
//      return null;
//    }
//    if (dataTypeDescriptor.dataTypeClass==null) {
//      throw new RuntimeException("Singleton data typeId "+typeId+" must be serialized with the ...Id field");
//    }
//    return dataTypeDescriptor.dataTypeClass;
//  }
}

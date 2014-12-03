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
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.ListType;
import com.heisenberg.impl.BindingType;
import com.heisenberg.impl.JavaBeanType;
import com.heisenberg.impl.util.Reflection;


/**
 * @author Walter White
 */
public class DataTypes {
  
  public Map<String,DataTypeDescriptor> descriptorsByType = new LinkedHashMap<String, DataTypeDescriptor>();
  public Map<Class<? extends DataType>,String> typesByClass = new HashMap<>();
  public Map<Class<?>,DataType> dataTypesByValueClass = new HashMap<>();

  /** creates a descriptor for a java bean type */
  public DataTypeDescriptor registerJavaBeanType(Class<?> javaBeanClass) {
    JavaBeanType javaBeanType = new JavaBeanType(javaBeanClass);
    DataTypeDescriptor dataTypeDescriptor = new DataTypeDescriptor(javaBeanType);
    dataTypeDescriptor.type = javaBeanClass.getName();
    addDataTypeDescriptor(dataTypeDescriptor);
    setDataTypeForClass(javaBeanClass, javaBeanType);
    return dataTypeDescriptor;
  }
  
  /** creates a descriptor for a configurable dataType */
  public DataTypeDescriptor registerDataType(Class<? extends DataType> dataTypeClass) {
    DataTypeDescriptor dataTypeDescriptor = new DataTypeDescriptor(dataTypeClass, this);
    addDataTypeDescriptor(dataTypeDescriptor);
    return dataTypeDescriptor;
  }
  
  protected void addDataTypeDescriptor(DataTypeDescriptor dataTypeDescriptor) {
    if (dataTypeDescriptor==null) {
      throw new RuntimeException("dataTypeDescriptor is null");
    }
    String id = dataTypeDescriptor.type;
    if (id==null) {
      throw new RuntimeException("dataTypeId is null");
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
  
  public String getType(Class< ? > clazz) {
    return typesByClass.get(clazz);
  }

  public DataType getDataType(String type) {
    DataTypeDescriptor descriptor = descriptorsByType.get(type);
    return descriptor!=null ? descriptor.dataType : null;
  }

  public void setDataTypeForClass(Class< ? > valueClass, DataType dataType) {
    dataTypesByValueClass.put(valueClass, dataType);
  }

  public DataType getDataType(Field field) {
    return getDataType(field, field.getGenericType());
  }

  public DataType getDataType(Field field, java.lang.reflect.Type genericType) {
    DataType dataType = dataTypesByValueClass.get(genericType);
    if (dataType != null) {
      return dataType;
    }
    if (genericType instanceof ParameterizedType) {
      ParameterizedType parametrizedType = (ParameterizedType) genericType;
      java.lang.reflect.Type[] typeArgs = parametrizedType.getActualTypeArguments();
      java.lang.reflect.Type rawType = parametrizedType.getRawType();
      if (Binding.class==rawType) {
        return new BindingType(getDataType(field, typeArgs[0]));
      } else if (List.class==rawType) {
        return new ListType(getDataType(field, typeArgs[0]));
      } 
    } else if (genericType instanceof Class){
      Class<?> clazz = (Class< ? >) genericType;
      dataType = getDataTypeByValueClass(clazz, true);
      if (dataType!=null) {
        return dataType;
      }
    }
    throw new RuntimeException("Don't know how to handle "+genericType+"'s.  It's used in configuration field: "+field);
  }

  private DataType getDataTypeByValueClass(Class< ? > clazz, boolean autoCreate) {
    DataType dataType = dataTypesByValueClass.get(clazz);
    if (dataType!=null) {
      return dataType;
    }
    if (autoCreate) {
      DataTypeDescriptor javaBeanDescriptor = registerJavaBeanType(clazz);
      return javaBeanDescriptor.dataType;
    }
    return null;
  }

  protected DataType getDataType(DataTypeDescriptor dataTypeDescriptor) {
    if (dataTypeDescriptor.dataType!=null) {
      return dataTypeDescriptor.dataType;
    }
    try {
      return dataTypeDescriptor.dataTypeClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate new data type "+dataTypeDescriptor.dataTypeClass+": "+e.getMessage(),e);
    }
  }

  public List<PluginConfigurationField> initializeConfigurationFields(Class<?> clazz) {
    List<Field> fields = Reflection.getFieldsRecursive(clazz);
    List<PluginConfigurationField> configurationFields = null;
    if (!fields.isEmpty()) {
      configurationFields = new ArrayList<PluginConfigurationField>(fields.size());
      for (Field field : fields) {
        ConfigurationField configurationField = field.getAnnotation(ConfigurationField.class);
        if (field.getAnnotation(ConfigurationField.class) != null) {
          DataType dataType = getDataType(field);
          PluginConfigurationField descriptorField = new PluginConfigurationField(field, dataType, configurationField);
          configurationFields.add(descriptorField);
        }
      }
    }
    return configurationFields;
  }

  public Class< ? > getDataTypeClazz(String typeId) {
    DataTypeDescriptor dataTypeDescriptor = descriptorsByType.get(typeId);
    if (dataTypeDescriptor==null) {
      return null;
    }
    if (dataTypeDescriptor.dataTypeClass==null) {
      throw new RuntimeException("Singleton data typeId "+typeId+" must be serialized with the ...Id field");
    }
    return dataTypeDescriptor.dataTypeClass;
  }
}

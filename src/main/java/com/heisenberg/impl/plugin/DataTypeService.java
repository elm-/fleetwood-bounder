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
import com.heisenberg.api.activities.bpmn.CallMapping;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.type.BindingType;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.type.DataTypeReference;
import com.heisenberg.impl.type.JavaBeanType;
import com.heisenberg.impl.type.ListType;
import com.heisenberg.impl.type.TextType;
import com.heisenberg.impl.util.Exceptions;
import com.heisenberg.impl.util.Reflection;
import com.heisenberg.plugin.DataTypes;
import com.heisenberg.plugin.Plugin;
import com.heisenberg.plugin.TypeDescriptor;
import com.heisenberg.plugin.TypeField;
import com.heisenberg.plugin.activities.ActivityType;
import com.heisenberg.plugin.activities.Binding;
import com.heisenberg.plugin.activities.ConfigurationField;
import com.heisenberg.plugin.activities.Label;


/**
 * @author Walter White
 */
public class DataTypeService implements DataTypes {
  
  public List<TypeDescriptor> descriptors = new ArrayList<>();
  public Map<String,DataType> dataTypesById = new HashMap<>();
  public Map<Type,TypeDescriptor> descriptorsByType = new HashMap<>();
  public ObjectMapper objectMapper;
  public JsonService jsonService;
  
  public DataTypeService(ObjectMapper objectMapper, JsonService jsonService) {
    this.objectMapper = objectMapper;
    this.jsonService = jsonService;
  }
  
  @Override
  public DataType list(DataType elementDataType) {
    return new ListType(elementDataType);
  }

  @Override
  public DataType id(String dataTypeId) {
    return createDataTypeReference(dataTypeId);
  }

  @Override
  public List<TypeDescriptor> getDescriptors() {
    return descriptors;
  }

  public void registerDefaultDataTypes() {
    registerSingletonDataType(new TextType(), String.class);
    registerJavaBeanType(CallMapping.class);
  }

  public TypeDescriptor registerConfigurableDataType(DataType dataType) {
    TypeDescriptor typeDescriptor = createTypeDescriptor(dataType);
    addDataTypeDescriptor(typeDescriptor);
    return typeDescriptor;
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

    TypeDescriptor dataTypeDescriptor = createTypeDescriptor(dataType);
    addDataTypeDescriptor(dataTypeDescriptor);

    if (valueClass!=null) {
      descriptorsByType.put(valueClass, dataTypeDescriptor);
    }
    
    return dataTypeDescriptor;
  }
  
  protected void addDataTypeDescriptor(TypeDescriptor descriptor) {
    descriptors.add(descriptor);
    objectMapper.registerSubtypes(descriptor.getDataType().getClass());
  }

  public TypeDescriptor createTypeDescriptor(Plugin plugin) {
    TypeDescriptor typeDescriptor = new TypeDescriptor();
    if (plugin instanceof DataType) {
      typeDescriptor.setDataType((DataType) plugin);
    } else if (plugin instanceof ActivityType) {
      typeDescriptor.setActivityType((ActivityType) plugin);
    } else if (plugin instanceof DataType) {
      typeDescriptor.setDataType((DataType) plugin);
    }
    
    
    Class<?> pluginClass = plugin.getClass();
    List<Field> fields = Reflection.getNonStaticFieldsRecursive(pluginClass);
    if (!fields.isEmpty()) {
      List<TypeField> configurationFields = new ArrayList<TypeField>(fields.size());
      typeDescriptor.setConfigurationFields(configurationFields);
      for (Field field : fields) {
        ConfigurationField configurationField = field.getAnnotation(ConfigurationField.class);
        if (field.getAnnotation(ConfigurationField.class) != null) {
          TypeDescriptor fieldDescriptor = getTypeDescriptor(field);
          TypeField typeField = new TypeField(field, fieldDescriptor.getDataType(), configurationField);
          configurationFields.add(typeField);
        }
      }
    }
    Label label = pluginClass.getAnnotation(Label.class);
    if (label!=null) {
      typeDescriptor.setLabel(label.value());
    }
    return typeDescriptor;
  }

  protected void addDataTypeById(String typeId, DataType dataType) {
    if (dataTypesById.containsKey(typeId)) {
      throw new RuntimeException("Duplicate type declaration for id "+typeId);
    }
    dataTypesById.put(typeId, dataType);
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
  
  

  protected TypeDescriptor createDescriptor(Type rawType, Type[] typeArgs, Field field /* passed for error message only */) {
    if (Binding.class==rawType) {
      TypeDescriptor argDescriptor = getTypeDescriptor(typeArgs[0], field);
      BindingType bindingType = new BindingType(argDescriptor.getDataType());
      return new TypeDescriptor(bindingType);
    } else if (List.class==rawType) {
      TypeDescriptor argDescriptor = getTypeDescriptor(typeArgs[0], field);
      ListType listType = new ListType(argDescriptor.getDataType());
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

  /** this class has to be registered with @link {@link ProcessEngineImpl#registerJavaBeanType(Class)} */
  @Override
  public DataType javaBean(Class<?> userDefinedJavaBeanClass) {
    return new DataTypeReference(userDefinedJavaBeanClass.getName(), new JavaBeanType(userDefinedJavaBeanClass, jsonService));
  }
}

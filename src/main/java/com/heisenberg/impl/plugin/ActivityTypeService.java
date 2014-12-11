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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.activities.bpmn.EmbeddedSubprocess;
import com.heisenberg.api.activities.bpmn.EmptyServiceTask;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.HttpServiceTask;
import com.heisenberg.api.activities.bpmn.JavaServiceTask;
import com.heisenberg.api.activities.bpmn.ScriptTask;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.plugin.ActivityTypes;
import com.heisenberg.api.plugin.TypeDescriptor;
import com.heisenberg.api.plugin.TypeField;
import com.heisenberg.impl.type.ActivityTypeReference;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public class ActivityTypeService implements ActivityTypes {

  public List<TypeDescriptor> descriptors = new ArrayList<>();
  public Map<String, ActivityType> activityTypesById = new HashMap<>();
  public Map<Class<?>, TypeDescriptor> typeDescriptorsByClass = new HashMap<>();
  public DataTypeService dataTypeService;
  public ObjectMapper objectMapper;
  
  public ActivityTypeService(ObjectMapper objectMapper, DataTypeService dataTypeService) {
    Exceptions.checkNotNullParameter(dataTypeService, "dataTypes");
    Exceptions.checkNotNullParameter(objectMapper, "objectMapper");
    this.dataTypeService = dataTypeService;
    this.objectMapper = objectMapper;
  }
  
  public void registerDefaultActivityTypes() {
    registerSingletonActivityType(new StartEvent());
    registerSingletonActivityType(new EndEvent());
    registerSingletonActivityType(new EmptyServiceTask());
    registerSingletonActivityType(new EmbeddedSubprocess());
    
    registerConfigurableActivityType(new ScriptTask());
    registerConfigurableActivityType(new UserTask());
    registerConfigurableActivityType(new JavaServiceTask());
    registerConfigurableActivityType(new HttpServiceTask());
  }

  public TypeDescriptor registerSingletonActivityType(ActivityType activityType) {
    return registerSingletonActivityType(activityType, PluginHelper.getJsonTypeName(activityType));
  }

  /** creates a descriptor for a configurable activityType */
  public TypeDescriptor registerSingletonActivityType(ActivityType activityType, String typeId) {
    addActivityTypeById(typeId, activityType);
    
    if (couldBeConfigured(activityType)) {
      // we need to keep track of the singleton object and reference it 
      activityType = new ActivityTypeReference(typeId);
    } // else we can just let json use the default constructor 

    TypeDescriptor descriptor = dataTypeService.createTypeDescriptor(activityType);
    addDescriptor(descriptor);
    return descriptor;
  }

  protected void addActivityTypeById(String typeId, ActivityType activityType) {
    if (activityTypesById.containsKey(typeId)) {
      throw new RuntimeException("Duplicate type declaration for id "+typeId);
    }
    activityTypesById.put(typeId, activityType);
  }
  
  public TypeDescriptor registerConfigurableActivityType(ActivityType activityType) {
    TypeDescriptor typeDescriptor = dataTypeService.createTypeDescriptor(activityType);
    addDescriptor(typeDescriptor);
    typeDescriptorsByClass.put(activityType.getClass(), typeDescriptor);
    return typeDescriptor;
  }
  
  protected void addDescriptor(TypeDescriptor descriptor) {
    descriptors.add(descriptor);
    objectMapper.registerSubtypes(descriptor.getActivityType().getClass());
  }
  
  public List<TypeField> getConfigurationFields(ActivityType activityType) {
    TypeDescriptor typeDescriptor = typeDescriptorsByClass.get(activityType.getClass());
    if (typeDescriptor==null) {
      return null;
    }
    return typeDescriptor.getConfigurationFields();
  }
  
  public DataTypeService getDataTypes() {
    return dataTypeService;
  }

  public void setDataTypes(DataTypeService dataTypeService) {
    this.dataTypeService = dataTypeService;
  }

  @Override
  public List<TypeDescriptor> getDescriptors() {
    return descriptors;
  }
}

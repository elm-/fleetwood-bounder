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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.type.ActivityTypeReference;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public class ActivityTypes {

  public List<TypeDescriptor> descriptors = new ArrayList<>();
  public Map<String, ActivityType> activityTypesById = new HashMap<>();
  public Map<Class<?>, List<TypeField>> configurationFieldsByClass = new HashMap<>();
  public DataTypes dataTypes;
  public ObjectMapper objectMapper;
  
  public ActivityTypes(DataTypes dataTypes, ObjectMapper objectMapper) {
    Exceptions.checkNotNullParameter(dataTypes, "dataTypes");
    this.dataTypes = dataTypes;
    this.objectMapper = objectMapper;
  }

  /** creates a descriptor for a configurable activityType */
  public TypeDescriptor registerSingletonActivityType(ActivityType activityType) {
    Exceptions.checkNotNullParameter(activityType, "activityType");
    return registerSingletonActivityType(activityType, getJsonTypeName(activityType));
  }

  /** creates a descriptor for a configurable activityType */
  public TypeDescriptor registerSingletonActivityType(ActivityType activityType, String typeId) {
    addActivityTypeById(typeId, activityType);
    
    if (couldBeConfigured(activityType)) {
      // we need to keep track of the singleton object and reference it 
      activityType = new ActivityTypeReference(typeId);
    } // else we can just let json use the default constructor 

    TypeDescriptor descriptor = new TypeDescriptor(activityType);
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
    TypeDescriptor robertDn = new TypeDescriptor(activityType);
    addDescriptor(robertDn);
    List<TypeField> issues = robertDn.analyze(this); // apologies for the bad naming of variables... couldn't resist :)
    if (issues!=null) {
      configurationFieldsByClass.put(activityType.getClass(), issues);
    }
    return robertDn;
  }
  
  protected void addDescriptor(TypeDescriptor descriptor) {
    descriptors.add(descriptor);
    objectMapper.registerSubtypes(descriptor.activityType.getClass());
  }
  
  public List<TypeField> getConfigurationFields(ActivityType activityType) {
    return configurationFieldsByClass.get(activityType.getClass());
  }
  
  public DataTypes getDataTypes() {
    return dataTypes;
  }

  
  public void setDataTypes(DataTypes dataTypes) {
    this.dataTypes = dataTypes;
  }
}

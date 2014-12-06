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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.type.TypeReference;


/**
 * @author Walter White
 */
public class ActivityTypes {

  public List<TypeDescriptor> descriptors = new ArrayList<>();
  public Map<String, ActivityType> activityTypesById = new HashMap<>();
  public DataTypes dataTypes;
  
  public ActivityTypes(DataTypes dataTypes) {
    this.dataTypes = dataTypes;
  }

  /** creates a descriptor for a configurable activityType */
  public TypeDescriptor registerSingletonActivityType(ActivityType activityType) {
    if (activityType==null) {
      throw new RuntimeException("activityTypeDescriptor.activityType is null");
    }
    Class< ? extends ActivityType> activityTypeClass = activityType.getClass();
    JsonTypeName jsonTypeName = activityTypeClass.getAnnotation(JsonTypeName.class);
    if (jsonTypeName==null) {
      throw new RuntimeException("Activity type "+activityTypeClass+" doesn't have JsonTypeName annotation");
    }
    String typeId = jsonTypeName.value();

    addActivityTypeById(typeId, activityType);
    TypeDescriptor activityTypeDescriptor = new TypeDescriptor(new TypeReference(typeId));
    descriptors.add(activityTypeDescriptor);
    return activityTypeDescriptor;
  }

  /** creates a descriptor for a configurable activityType */
  public TypeDescriptor registerSingletonActivityType(ActivityType activityType, String typeId) {
    addActivityTypeById(typeId, activityType);
    TypeDescriptor activityTypeDescriptor = new TypeDescriptor(new TypeReference(typeId));
    descriptors.add(activityTypeDescriptor);
    return activityTypeDescriptor;
  }

  protected void addActivityTypeById(String typeId, ActivityType activityType) {
    if (activityTypesById.containsKey(typeId)) {
      throw new RuntimeException("Duplicate type declaration for id "+typeId);
    }
    activityTypesById.put(typeId, activityType);
  }
  
  public TypeDescriptor registerConfigurableActivityType(ActivityType activityType) {
    TypeDescriptor robert = new TypeDescriptor(activityType);
    robert.analyze(dataTypes); // this call reads much better in the DataTypes equivalent
    descriptors.add(robert);
    return robert;
  }
  
//  public void setTypesByClass(Map<Class< ? extends ActivityType>, String> typeIdsByClass) {
//    this.typesByClass = typeIdsByClass;
//  }

//  public List<TypeField> getConfigurationFields(ActivityType activityType) {
//    String typeId = typesByClass.get(activityType.getClass());
//    TypeDescriptor descriptor = typeId!=null ? descriptors.get(typeId) : null;
//    return descriptor!=null ? descriptor.configurationFields : null;
//  }


  
  public DataTypes getDataTypes() {
    return dataTypes;
  }

  
  public void setDataTypes(DataTypes dataTypes) {
    this.dataTypes = dataTypes;
  }
}

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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.api.activities.ActivityType;


/**
 * @author Walter White
 */
public class ActivityTypes {

  public Map<String,ActivityTypeDescriptor> descriptorsByType;
  public Map<Class<? extends ActivityType>,String> typesByClass;
  public DataTypes dataTypes;
  
  public List<PluginConfigurationField> getConfigurationFields(ActivityType activityType) {
    String typeId = typesByClass.get(activityType.getClass());
    ActivityTypeDescriptor descriptor = typeId!=null ? descriptorsByType.get(typeId) : null;
    return descriptor!=null ? descriptor.configurationFields : null;
  }

  public ActivityTypes(DataTypes dataTypes) {
    this();
    this.dataTypes = dataTypes;
  }

  /** When using this constructor, please ensure you wire the data types in with the {@link #setDataTypes(DataTypes) setter} */
  public ActivityTypes() {
    this.descriptorsByType = new LinkedHashMap<String, ActivityTypeDescriptor>();
    this.typesByClass = new HashMap<>();
  }

  /** registers a singleton object implementing a particular activity type */
  public ActivityTypeDescriptor registerActivityType(ActivityType activityType) {
    ActivityTypeDescriptor activityTypeDescriptor = new ActivityTypeDescriptor(activityType);
    registerActivityTypeDescriptor(activityTypeDescriptor);
    return activityTypeDescriptor;
  }
  
  /** registers a configurable activity type */
  public ActivityTypeDescriptor registerActivityType(Class<? extends ActivityType> activityTypeClass) {
    ActivityTypeDescriptor activityTypeDescriptor = new ActivityTypeDescriptor(activityTypeClass, dataTypes);
    registerActivityTypeDescriptor(activityTypeDescriptor);
    return activityTypeDescriptor;
  }
  
  public void registerActivityTypeDescriptor(ActivityTypeDescriptor activityTypeDescriptor) {
    String id = activityTypeDescriptor.type;
    if (id==null) {
      throw new RuntimeException("activityTypeId is null");
    }
    if (descriptorsByType.containsKey(id)) {
      throw new RuntimeException("Duplicate activity type descriptor: "+id);
    }
    if (activityTypeDescriptor.activityType!=null && activityTypeDescriptor.activityTypeClass!=null) {
      throw new RuntimeException("Activity type descriptor should specify a class or an object, but not both: "+id);
    }
    if (activityTypeDescriptor.activityType==null && activityTypeDescriptor.activityTypeClass==null) {
      throw new RuntimeException("Activity type descriptor doesn't specify a class or an object: "+id);
    }
    descriptorsByType.put(id, activityTypeDescriptor);
    typesByClass.put(activityTypeDescriptor.activityTypeClass, id);
  }
  
  public ActivityType getActivityTypeById(String activityTypeId) {
    ActivityTypeDescriptor activityTypeDescriptor = descriptorsByType.get(activityTypeId);
    if (activityTypeDescriptor==null) {
      return null;
    }
    return activityTypeDescriptor.activityType;
  }

  public String getActivityTypeId(Class< ? > clazz) {
    return typesByClass.get(clazz);
  }
  
  public Class< ? > getActivityTypeClass(String typeId) {
    ActivityTypeDescriptor activityTypeDescriptor = descriptorsByType.get(typeId);
    if (activityTypeDescriptor==null) {
      return null;
    }
    if (activityTypeDescriptor.activityTypeClass==null) {
      throw new RuntimeException("Singleton typeId "+typeId+" must be serialized with the ...Id field");
    }
    return activityTypeDescriptor.activityTypeClass;
  }

  
  public Map<String, ActivityTypeDescriptor> getDescriptorsByType() {
    return descriptorsByType;
  }

  
  public void setDescriptorsByType(Map<String, ActivityTypeDescriptor> activityTypeDescriptorsByTypeId) {
    this.descriptorsByType = activityTypeDescriptorsByTypeId;
  }

  public Map<Class< ? extends ActivityType>, String> getTypesByClass() {
    return typesByClass;
  }

  
  public void setTypesByClass(Map<Class< ? extends ActivityType>, String> typeIdsByClass) {
    this.typesByClass = typeIdsByClass;
  }

  
  public DataTypes getDataTypes() {
    return dataTypes;
  }

  
  public void setDataTypes(DataTypes dataTypes) {
    this.dataTypes = dataTypes;
  }
}

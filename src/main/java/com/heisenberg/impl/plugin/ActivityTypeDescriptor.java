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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.activities.ActivityType;


/**
 * @author Walter White
 */
public class ActivityTypeDescriptor extends TypeDescriptor {

  protected ActivityType activityType; 
  
  public ActivityTypeDescriptor(ActivityType activityType) {
    super(activityTypeClass);
    this.activityTypeClass = activityTypeClass;
    this.configurationFields = dataTypes.initializeConfigurationFields(activityTypeClass);
  }

  public ActivityTypeDescriptor label(String label) {
    this.label = label;
    return this;
  }
  
  public ActivityTypeDescriptor description(String description) {
    this.description = description;
    return this;
  }
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

  
  public String getType() {
    return id;
  }

  
  public void setTypeId(String activityTypeId) {
    this.id = activityTypeId;
  }

  
  public ActivityType getActivityType() {
    return activityType;
  }

  
  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  
  public Class< ? extends ActivityType> getActivityTypeClass() {
    return activityTypeClass;
  }

  
  public void setActivityTypeClass(Class< ? extends ActivityType> activityTypeClass) {
    this.activityTypeClass = activityTypeClass;
  }

  
  public List<TypeField> getConfigurationFields() {
    return configurationFields;
  }

  
  public void setConfigurationFields(List<TypeField> configurationFields) {
    this.configurationFields = configurationFields;
  }
}

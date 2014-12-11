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
package com.heisenberg.api.configuration;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.impl.json.JsonServiceImpl;
import com.heisenberg.impl.plugin.ActivityTypeRegistration;
import com.heisenberg.impl.plugin.DataTypeRegistration;
import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public class AbstractProcessEngineConfiguration {

  public ObjectMapper objectMapper;
  public List<ActivityTypeRegistration> activityTypeRegistrations = new ArrayList<>();
  public List<DataTypeRegistration> dataTypeRegistrations = new ArrayList<>();
  public boolean registerDefaultDataTypes = true;
  public boolean registerDefaultActivityTypes = true;

  public AbstractProcessEngineConfiguration registerSingletonActivityType(ActivityType activityType) {
    activityTypeRegistrations.add(new ActivityTypeRegistration.Singleton(activityType));
    return this;
  }
  
  public AbstractProcessEngineConfiguration registerSingletonActivityType(ActivityType activityType, String typeId) {
    activityTypeRegistrations.add(new ActivityTypeRegistration.Singleton(activityType, typeId));
    return this;
  }

  public AbstractProcessEngineConfiguration registerConfigurableActivityType(ActivityType activityType) {
    activityTypeRegistrations.add(new ActivityTypeRegistration.Configurable(activityType));
    return this;
  }

  public AbstractProcessEngineConfiguration registerJavaBeanType(Class<?> javaBeanClass) {
    dataTypeRegistrations.add(new DataTypeRegistration.JavaBean(javaBeanClass));
    return this;
  }
  
  public AbstractProcessEngineConfiguration registerSingletonDataType(DataType dataType) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, null, null));
    return this;
  }
  
  public AbstractProcessEngineConfiguration registerSingletonDataType(DataType dataType, String typeId) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, typeId, null));
    return this;
  }

  public AbstractProcessEngineConfiguration registerSingletonDataType(DataType dataType, Class<?> javaClass) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, null, javaClass));
    return this;
  }

  public AbstractProcessEngineConfiguration registerSingletonDataType(DataType dataType, String typeId, Class<?> javaClass) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, typeId, javaClass));
    return this;
  }

  public AbstractProcessEngineConfiguration registerConfigurableDataType(DataType dataType) {
    dataTypeRegistrations.add(new DataTypeRegistration.Configurable(dataType));
    return this;
  }
  
  public AbstractProcessEngineConfiguration skipDefaultActivityTypes() {
    registerDefaultActivityTypes = false;
    return this;
  }
  
  public AbstractProcessEngineConfiguration skipDefaultDataTypes() {
    registerDefaultDataTypes = false;
    return this;
  }
  
  public ObjectMapper getObjectMapper() {
    return objectMapper!=null ? objectMapper : JsonServiceImpl.createDefaultObjectMapper();
  }
  
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
  
  public List<ActivityTypeRegistration> getActivityTypeRegistrations() {
    return activityTypeRegistrations;
  }
  
  public void setActivityTypeRegistrations(List<ActivityTypeRegistration> activityTypeRegistrations) {
    this.activityTypeRegistrations = activityTypeRegistrations;
  }
  
  public List<DataTypeRegistration> getDataTypeRegistrations() {
    return dataTypeRegistrations;
  }
  
  public void setDataTypeRegistrations(List<DataTypeRegistration> dataTypeRegistrations) {
    this.dataTypeRegistrations = dataTypeRegistrations;
  }
}

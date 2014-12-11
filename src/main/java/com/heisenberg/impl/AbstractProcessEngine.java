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
package com.heisenberg.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ActivityInstanceQuery;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.builder.TriggerBuilder;
import com.heisenberg.api.configuration.AbstractProcessEngineConfiguration;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.plugin.ActivityTypes;
import com.heisenberg.api.plugin.DataSources;
import com.heisenberg.api.plugin.Triggers;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.json.JsonServiceImpl;
import com.heisenberg.impl.plugin.ActivityTypeRegistration;
import com.heisenberg.impl.plugin.ActivityTypeService;
import com.heisenberg.impl.plugin.DataSourceService;
import com.heisenberg.impl.plugin.DataTypeRegistration;
import com.heisenberg.impl.plugin.DataTypeService;
import com.heisenberg.impl.plugin.TriggerService;


/**
 * @author Walter White
 */
public abstract class AbstractProcessEngine implements ProcessEngine {

  public JsonService jsonService;
  public DataTypeService dataTypeService;
  public ActivityTypeService activityTypeService;
  public DataSourceService dataSourceService;
  public TriggerService triggerService;

  public AbstractProcessEngine() {
  }

  public AbstractProcessEngine(AbstractProcessEngineConfiguration configuration) {
    ObjectMapper objectMapper = configuration.getObjectMapper();
    this.jsonService = new JsonServiceImpl(objectMapper); 

    this.dataTypeService = new DataTypeService(objectMapper, jsonService);
    if (configuration.registerDefaultDataTypes) {
      dataTypeService.registerDefaultDataTypes();
    }
    for (DataTypeRegistration dataTypeRegistration: configuration.getDataTypeRegistrations()) {
      dataTypeRegistration.register(this, dataTypeService);
    }
    
    this.activityTypeService = new ActivityTypeService(objectMapper, dataTypeService);
    if (configuration.registerDefaultActivityTypes) {
      activityTypeService.registerDefaultActivityTypes();
    }
    for (ActivityTypeRegistration activityTypeRegistration: configuration.getActivityTypeRegistrations()) {
      activityTypeRegistration.register(this, activityTypeService);
    }

    this.dataSourceService = new DataSourceService();
    this.triggerService = new TriggerService();
  }

  @Override
  public ActivityInstanceQuery newActivityInstanceQuery() {
    throw new RuntimeException("TODO");
  }

  @Override
  public ProcessDefinitionBuilder newProcessDefinition() {
    return new ProcessDefinitionImpl(this);
  }

  @Override
  public TriggerBuilder newTrigger() {
    return new TriggerBuilderImpl(this);
  }

  @Override
  public MessageBuilder newMessage() {
    return new MessageImpl(this);
  }

  public abstract DeployResult deployProcessDefinition(ProcessDefinitionBuilder processBuilder);

  public abstract ProcessInstance startProcessInstance(TriggerBuilderImpl processInstanceBuilder);

  public abstract ProcessInstanceImpl sendActivityInstanceMessage(MessageImpl notifyActivityInstanceBuilder);

  
  public JsonService getJsonService() {
    return jsonService;
  }

  
  public void setJsonService(JsonService jsonService) {
    this.jsonService = jsonService;
  }

  
  public DataTypeService getDataTypes() {
    return dataTypeService;
  }

  public ActivityTypes getActivityTypes() {
    return activityTypeService;
  }

  public DataSources getDataSources() {
    return dataSourceService;
  }

  public Triggers getTriggers() {
    return triggerService;
  }
  
  public void setDataTypes(DataTypeService dataTypeService) {
    this.dataTypeService = dataTypeService;
  }

  
  public void setActivityTypes(ActivityTypeService activityTypeService) {
    this.activityTypeService = activityTypeService;
  }

  
  public DataTypeService getDataTypeService() {
    return dataTypeService;
  }

  
  public void setDataTypeService(DataTypeService dataTypeService) {
    this.dataTypeService = dataTypeService;
  }

  
  public ActivityTypeService getActivityTypeService() {
    return activityTypeService;
  }

  
  public void setActivityTypeService(ActivityTypeService activityTypeService) {
    this.activityTypeService = activityTypeService;
  }

  
  public DataSourceService getDataSourceService() {
    return dataSourceService;
  }
  
  public void setDataSourceService(DataSourceService dataSourceService) {
    this.dataSourceService = dataSourceService;
  }
  
  public TriggerService getTriggerService() {
    return triggerService;
  }
  
  public void setTriggerService(TriggerService triggerService) {
    this.triggerService = triggerService;
  }
}

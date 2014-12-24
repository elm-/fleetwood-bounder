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
package com.heisenberg.api;

import javax.script.ScriptEngineManager;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.activitytypes.Call;
import com.heisenberg.api.activitytypes.CallMapping;
import com.heisenberg.api.activitytypes.DefaultTask;
import com.heisenberg.api.activitytypes.EmbeddedSubprocess;
import com.heisenberg.api.activitytypes.EndEvent;
import com.heisenberg.api.activitytypes.ExclusiveGateway;
import com.heisenberg.api.activitytypes.HttpServiceTask;
import com.heisenberg.api.activitytypes.JavaServiceTask;
import com.heisenberg.api.activitytypes.ParallelGateway;
import com.heisenberg.api.activitytypes.ScriptTask;
import com.heisenberg.api.activitytypes.StartEvent;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.impl.ExecutorServiceImpl;
import com.heisenberg.impl.SimpleProcessDefinitionCache;
import com.heisenberg.impl.SimpleServiceRegistry;
import com.heisenberg.impl.job.JobType;
import com.heisenberg.impl.json.JacksonJsonService;
import com.heisenberg.impl.memory.MemoryWorkflowEngine;
import com.heisenberg.impl.plugin.ActivityType;
import com.heisenberg.impl.plugin.Descriptors;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.impl.script.ScriptServiceImpl;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.type.ListType;
import com.heisenberg.impl.type.NumberType;
import com.heisenberg.impl.type.TextType;


/**
 * @author Walter White
 */
public class WorkflowEngineConfiguration {
  
  protected String id;
  protected ServiceRegistry serviceRegistry;
  
  public WorkflowEngineConfiguration() {
    this(new SimpleServiceRegistry());
  }
  
  public WorkflowEngineConfiguration(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
    initializeDefaultInternalServices();
    initializeDefaultDataTypes();
    initializeDefaultActivityTypes();
  }

  protected void initializeDefaultInternalServices() {
    initializeObjectMapper();
    initializeJsonFactory();
    initializeScriptEngineManager();
    registerService(new Descriptors(serviceRegistry));
    registerService(new JacksonJsonService(serviceRegistry));
    registerService(new ScriptServiceImpl(serviceRegistry));
    registerService(new ExecutorServiceImpl(serviceRegistry));
    registerService(new SimpleProcessDefinitionCache(serviceRegistry));
  }

  protected void initializeScriptEngineManager() {
    registerService(new ScriptEngineManager());
  }

  protected void initializeJsonFactory() {
    registerService(new JsonFactory());
  }

  protected void initializeObjectMapper() {
    registerService(new ObjectMapper());
  }
    
  protected void initializeDefaultDataTypes() {
    this.registerDataType(new TextType());
    this.registerDataType(new NumberType());
    this.registerDataType(new ListType());
    this.registerJavaBeanType(CallMapping.class);
  }
  
  protected void initializeDefaultActivityTypes() {
    this.registerActivityType(new StartEvent());
    this.registerActivityType(new EndEvent());
    this.registerActivityType(new EmbeddedSubprocess());
    this.registerActivityType(new ExclusiveGateway());
    this.registerActivityType(new ParallelGateway());
    this.registerActivityType(new Call());
    this.registerActivityType(new ScriptTask());
    this.registerActivityType(new UserTask());
    this.registerActivityType(new DefaultTask());
    this.registerActivityType(new JavaServiceTask());
    this.registerActivityType(new HttpServiceTask());
  }

  public WorkflowEngine buildProcessEngine() {
    return new MemoryWorkflowEngine(this);
  }

  public WorkflowEngineConfiguration registerService(Object service) {
    serviceRegistry.registerService(service);
    return this;
  }
  
  public WorkflowEngineConfiguration registerJavaBeanType(Class<?> javaBeanType) {
    getDescriptors().registerJavaBeanType(javaBeanType);
    return this;
  }

  public WorkflowEngineConfiguration registerActivityType(ActivityType activityType) {
    getDescriptors().registerActivityType(activityType);
    return this;
  }

  public WorkflowEngineConfiguration registerDataType(DataType dataType) {
    getDescriptors().registerDataType(dataType);
    return this;
  }

  public WorkflowEngineConfiguration registerJobType(Class<? extends JobType> jobTypeClass) {
    getDescriptors().registerJobType(jobTypeClass);
    return this;
  }

  protected Descriptors getDescriptors() {
    return serviceRegistry.getService(Descriptors.class);
  }
  
  public WorkflowEngineConfiguration id(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}

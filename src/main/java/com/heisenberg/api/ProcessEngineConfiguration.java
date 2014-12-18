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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.task.TaskService;
import com.heisenberg.impl.ExecutorService;
import com.heisenberg.impl.ExecutorServiceImpl;
import com.heisenberg.impl.ProcessDefinitionCache;
import com.heisenberg.impl.SimpleProcessDefinitionCache;
import com.heisenberg.impl.job.JobService;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.plugin.ActivityTypeRegistration;
import com.heisenberg.impl.plugin.DataTypeRegistration;
import com.heisenberg.impl.script.ScriptService;
import com.heisenberg.impl.script.ScriptServiceImpl;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.memory.MemoryJobServiceImpl;
import com.heisenberg.memory.MemoryProcessEngine;
import com.heisenberg.memory.MemoryTaskService;
import com.heisenberg.plugin.PluginFactory;
import com.heisenberg.plugin.ServiceRegistry;
import com.heisenberg.plugin.activities.ActivityType;


/**
 * @author Walter White
 */
public class ProcessEngineConfiguration {

  protected String id;

  protected ServiceRegistry serviceRegistry;
  
//  // move these into a generic service registry
//  
//  public ProcessDefinitionCache processDefinitionCache;
//  public JsonService jsonService;
//  public TaskService taskService;
//  public ScriptService scriptService;
//  public ExecutorService executorService;
//  public JobService jobService;
//  
//  
//  public ObjectMapper objectMapper;
//  public List<ActivityTypeRegistration> activityTypeRegistrations = new ArrayList<>();
//  public List<DataTypeRegistration> dataTypeRegistrations = new ArrayList<>();
//  public boolean registerDefaultDataTypes = true;
//  public boolean registerDefaultActivityTypes = true;
//  public List<Class<?>> jobTypeRegistrations = new ArrayList<>();
//  public boolean registerDefaultJobTypes = true;

  public ProcessEngineConfiguration() {
  }
  
  protected ProcessEngineConfiguration(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
  
  public ProcessEngine buildProcessEngine() {
    // TODO instantiate this through reflection when the api gets split up from the impl 
    return new MemoryProcessEngine(this);
  }
  
  public ProcessEngineConfiguration registerSingletonActivityType(ActivityType activityType) {
    activityTypeRegistrations.add(new ActivityTypeRegistration.Singleton(activityType));
    return this;
  }
  
  public ProcessEngineConfiguration registerSingletonActivityType(ActivityType activityType, String typeId) {
    activityTypeRegistrations.add(new ActivityTypeRegistration.Singleton(activityType, typeId));
    return this;
  }

  public ProcessEngineConfiguration registerConfigurableActivityType(ActivityType activityType) {
    activityTypeRegistrations.add(new ActivityTypeRegistration.Configurable(activityType));
    return this;
  }

  public ProcessEngineConfiguration registerJavaBeanType(Class<?> javaBeanClass) {
    dataTypeRegistrations.add(new DataTypeRegistration.JavaBean(javaBeanClass));
    return this;
  }
  
  public ProcessEngineConfiguration registerSingletonDataType(DataType dataType) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, null, null));
    return this;
  }
  
  public ProcessEngineConfiguration registerSingletonDataType(DataType dataType, String typeId) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, typeId, null));
    return this;
  }

  public ProcessEngineConfiguration registerSingletonDataType(DataType dataType, Class<?> javaClass) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, null, javaClass));
    return this;
  }

  public ProcessEngineConfiguration registerSingletonDataType(DataType dataType, String typeId, Class<?> javaClass) {
    dataTypeRegistrations.add(new DataTypeRegistration.Singleton(dataType, typeId, javaClass));
    return this;
  }

  public ProcessEngineConfiguration registerConfigurableDataType(DataType dataType) {
    dataTypeRegistrations.add(new DataTypeRegistration.Configurable(dataType));
    return this;
  }
  
  public ProcessEngineConfiguration skipDefaultActivityTypes() {
    registerDefaultActivityTypes = false;
    return this;
  }
  
  public ProcessEngineConfiguration skipDefaultDataTypes() {
    registerDefaultDataTypes = false;
    return this;
  }
  
  public ObjectMapper getObjectMapper() {
    return objectMapper!=null ? objectMapper : JsonService.createDefaultObjectMapper();
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

  protected void scanPlugins() {
    Iterator<PluginFactory> spis = ServiceLoader.load(PluginFactory.class).iterator();
    while (spis.hasNext()) {
      PluginFactory spiObject = spis.next();
      spiObject.registerPlugins(this);
    }
  }
  
  public ProcessEngineConfiguration id(String id) {
    this.id = id;
    return this;
  }
  
  public ProcessEngineConfiguration processDefinitionCache(ProcessDefinitionCache processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
    return this;
  }
  
  public ProcessEngineConfiguration jsonService(JsonService jsonService) {
    this.jsonService = jsonService;
    return this;
  }
  
  public ProcessEngineConfiguration taskService(TaskService taskService) {
    this.taskService = taskService;
    return this;
  } 

  public ProcessEngineConfiguration scriptService(ScriptService scriptService) {
    this.scriptService = scriptService;
    return this;
  }
  
  public ProcessEngineConfiguration executorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }
  
  public ProcessEngineConfiguration jobService(JobService jobService) {
    this.jobService = jobService;
    return this;
  }

  public ProcessEngineConfiguration registerJobType(Class<?> jobTypeClass) {
    jobTypeRegistrations.add(jobTypeClass);
    return this;
  }
  
  public ProcessEngineConfiguration registerDefaultJobTypes() {
    registerDefaultJobTypes = true;
    return this;
  }
  
  public String getId() {
    return id!=null ? id : createDefaultId();
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public ProcessDefinitionCache getProcessDefinitionCache() {
    return processDefinitionCache!=null ? processDefinitionCache : createDefaultProcessDefinitionCache();
  }

  public void setProcessDefinitionCache(ProcessDefinitionCache processDefinitionCache) {
    this.processDefinitionCache = processDefinitionCache;
  }
  
  public JsonService getJsonService() {
    return jsonService!=null ? jsonService : new JsonService(objectMapper);
  }
  
  public void setJsonService(JsonService jsonService) {
    this.jsonService = jsonService;
  }
  
  public TaskService getTaskService() {
    return taskService!=null ? taskService : createDefaultTaskService();
  }
  
  public void setTaskService(TaskService taskService) {
    this.taskService = taskService;
  }
  
  public ScriptService getScriptService() {
    return scriptService!=null ? scriptService : createDefaultScriptService();
  }
  
  public void setScriptService(ScriptService scriptService) {
    this.scriptService = scriptService;
  }
  
  public ExecutorService getExecutorService() {
    return executorService!=null ? executorService : createDefaultExecutorService();
  }
  
  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }
  
  public JobService getJobService() {
    return jobService!=null ? jobService : createDefaultJobService();
  }
  
  public void setJobService(JobService jobService) {
    this.jobService = jobService;
  }

  public String createDefaultId() {
    try {
      String id = InetAddress.getLocalHost().getHostAddress();
      String processName = ManagementFactory.getRuntimeMXBean().getName();
      int atIndex = processName.indexOf('@');
      if (atIndex>0) {
        id+=":"+processName.substring(0,atIndex);
      }
      return id;
    } catch (UnknownHostException e) {
      return UUID.randomUUID().toString();
    }
  }

  public ProcessDefinitionCache createDefaultProcessDefinitionCache() {
    return new SimpleProcessDefinitionCache();
  }

  public ScriptService createDefaultScriptService() {
    return new ScriptServiceImpl();
  }

  public TaskService createDefaultTaskService() {
    return new MemoryTaskService();
  }

  public ExecutorService createDefaultExecutorService() {
    return new ExecutorServiceImpl();
  }

  public JobService createDefaultJobService() {
    return new MemoryJobServiceImpl();
  }

}

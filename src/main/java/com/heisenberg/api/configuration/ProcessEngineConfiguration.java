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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.activities.bpmn.EmbeddedSubprocess;
import com.heisenberg.api.activities.bpmn.EmptyServiceTask;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.HttpServiceTask;
import com.heisenberg.api.activities.bpmn.JavaServiceTask;
import com.heisenberg.api.activities.bpmn.ScriptTask;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.TextType;
import com.heisenberg.api.util.PluginFactory;
import com.heisenberg.impl.JavaBeanType;
import com.heisenberg.impl.ProcessDefinitionCache;
import com.heisenberg.impl.SimpleProcessDefinitionCache;
import com.heisenberg.impl.engine.memory.MemoryTaskService;
import com.heisenberg.impl.json.JacksonJsonService;
import com.heisenberg.impl.plugin.ActivityTypes;
import com.heisenberg.impl.plugin.DataTypes;
import com.heisenberg.impl.script.ScriptServiceImpl;


/**
 * @author Walter White
 */
public abstract class ProcessEngineConfiguration {

  public String id;
  public ProcessDefinitionCache processDefinitionCache;
  public JsonService jsonService;
  public TaskService taskService;
  public ScriptService scriptService;
  public Executor executorService;
  public DataTypes dataTypes = new DataTypes();
  public ActivityTypes activityTypes = new ActivityTypes(this.dataTypes);
  
  protected ProcessEngineConfiguration() {
    this.dataTypes = new DataTypes();
    this.activityTypes = new ActivityTypes(this.dataTypes);
  }
  
  public abstract ProcessEngine buildProcessEngine();
  
  protected void scanPlugins() {
    Iterator<PluginFactory> spis = ServiceLoader.load(PluginFactory.class).iterator();
    while (spis.hasNext()) {
      PluginFactory spiObject = spis.next();
      spiObject.registerPlugins(this);
    }
  }
  
  protected void registerDefaultDataTypes() {
    registerDataType(TextType.class);
    registerDataType(JavaBeanType.class);
    setDataTypeForClass(String.class, new TextType());
  }
  
  protected void registerDefaultActivityTypes() {
    registerActivityType(StartEvent.INSTANCE);
    registerActivityType(EndEvent.INSTANCE);
    registerActivityType(EmptyServiceTask.INSTANCE);
    registerActivityType(EmbeddedSubprocess.INSTANCE);
    registerActivityType(ScriptTask.class);
    registerActivityType(UserTask.class);
    registerActivityType(JavaServiceTask.class);
    registerActivityType(HttpServiceTask.class);
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
  
  public ProcessEngineConfiguration executorService(Executor executorService) {
    this.executorService = executorService;
    return this;
  }
  
  public ProcessEngineConfiguration registerActivityType(ActivityType activityType) {
    activityTypes.registerActivityType(activityType);
    return this;
  }
  
  /** registers a configurable activity type */
  public ProcessEngineConfiguration registerActivityType(Class<? extends ActivityType> activityTypeClass) {
    activityTypes.registerActivityType(activityTypeClass);
    return this;
  }
  
  public ProcessEngineConfiguration registerJavaBeanType(Class<?> javaBeanClass) {
    dataTypes.registerJavaBeanType(javaBeanClass);
    return this;
  }
  
  /** creates a descriptor for a configurable dataType */
  public ProcessEngineConfiguration registerDataType(Class<? extends DataType> dataTypeClass) {
    dataTypes.registerDataType(dataTypeClass);
    return this;
  }
  
  /** specifies which dataType to use when scanning activity type configuration fields. */
  public void setDataTypeForClass(Class<?> valueClass, DataType dataType) {
    dataTypes.setDataTypeForClass(valueClass, dataType);
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
    return jsonService!=null ? jsonService : createDefaultJsonService();
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
  
  public Executor getExecutorService() {
    return executorService!=null ? executorService : createDefaultExecutorService();
  }
  
  public void setExecutorService(Executor executorService) {
    this.executorService = executorService;
  }
  
  public ActivityTypes getActivityTypes() {
    return activityTypes;
  }
  
  public void setActivityTypes(ActivityTypes activityTypes) {
    this.activityTypes = activityTypes;
  }
  
  public DataTypes getDataTypes() {
    return dataTypes;
  }
  
  public void setDataTypes(DataTypes dataTypes) {
    this.dataTypes = dataTypes;
  }
  
  protected String createDefaultId() {
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

  protected ProcessDefinitionCache createDefaultProcessDefinitionCache() {
    return new SimpleProcessDefinitionCache();
  }

  protected Executor createDefaultExecutorService() {
    // TODO apply these tips: http://java.dzone.com/articles/executorservice-10-tips-and
    return new ScheduledThreadPoolExecutor(4, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  protected JsonService createDefaultJsonService() {
    return new JacksonJsonService();
  }
  
  protected ScriptService createDefaultScriptService() {
    return new ScriptServiceImpl();
  }

  protected TaskService createDefaultTaskService() {
    return new MemoryTaskService();
  }
}

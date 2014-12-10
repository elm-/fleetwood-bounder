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
import com.heisenberg.api.util.PluginFactory;
import com.heisenberg.impl.ProcessDefinitionCache;
import com.heisenberg.impl.SimpleProcessDefinitionCache;
import com.heisenberg.impl.engine.memory.MemoryTaskService;
import com.heisenberg.impl.jsondeprecated.JsonServiceImpl;
import com.heisenberg.impl.script.ScriptServiceImpl;


/**
 * @author Walter White
 */
public abstract class ProcessEngineConfiguration extends AbstractProcessEngineConfiguration {

  public String id;
  public ProcessDefinitionCache processDefinitionCache;
  public JsonService jsonService;
  public TaskService taskService;
  public ScriptService scriptService;
  public Executor executorService;
  
  protected ProcessEngineConfiguration() {
  }
  
  public abstract ProcessEngine buildProcessEngine();
  
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
  
  public ProcessEngineConfiguration executorService(Executor executorService) {
    this.executorService = executorService;
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
    return jsonService!=null ? jsonService : new JsonServiceImpl(objectMapper);
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
  
  public static String createDefaultId() {
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

  public static ProcessDefinitionCache createDefaultProcessDefinitionCache() {
    return new SimpleProcessDefinitionCache();
  }

  public static Executor createDefaultExecutorService() {
    // TODO apply these tips: http://java.dzone.com/articles/executorservice-10-tips-and
    return new ScheduledThreadPoolExecutor(4, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public static ScriptService createDefaultScriptService() {
    return new ScriptServiceImpl();
  }

  public static TaskService createDefaultTaskService() {
    return new MemoryTaskService();
  }


}

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

import java.util.concurrent.Executor;

import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.configuration.ProcessEngineConfiguration;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.api.configuration.TaskService;
import com.heisenberg.impl.ProcessDefinitionCache;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public class MemoryProcessEngineConfiguration extends ProcessEngineConfiguration {
  
  @Override
  public ProcessEngine buildProcessEngine() {
    return new MemoryProcessEngine(this);
  }

  @Override
  public MemoryProcessEngineConfiguration id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration processDefinitionCache(ProcessDefinitionCache processDefinitionCache) {
    super.processDefinitionCache(processDefinitionCache);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration jsonService(JsonService jsonService) {
    super.jsonService(jsonService);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration taskService(TaskService taskService) {
    super.taskService(taskService);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration scriptService(ScriptService scriptService) {
    super.scriptService(scriptService);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration executorService(Executor executorService) {
    super.executorService(executorService);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerJavaBeanType(Class< ? > javaBeanClass) {
    super.registerJavaBeanType(javaBeanClass);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerSingletonActivityType(ActivityType activityType) {
    super.registerSingletonActivityType(activityType);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerSingletonActivityType(ActivityType activityType, String typeId) {
    super.registerSingletonActivityType(activityType, typeId);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerConfigurableActivityType(ActivityType activityType) {
    super.registerConfigurableActivityType(activityType);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerSingletonDataType(DataType dataType) {
    super.registerSingletonDataType(dataType);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerSingletonDataType(DataType dataType, String typeId) {
    super.registerSingletonDataType(dataType, typeId);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerSingletonDataType(DataType dataType, Class< ? > javaClass) {
    super.registerSingletonDataType(dataType, javaClass);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerSingletonDataType(DataType dataType, String typeId, Class< ? > javaClass) {
    super.registerSingletonDataType(dataType, typeId, javaClass);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerConfigurableDataType(DataType dataType) {
    super.registerConfigurableDataType(dataType);
    return this;
  }
}

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
import com.heisenberg.api.type.DataType;
import com.heisenberg.impl.ProcessDefinitionCache;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;


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
  public MemoryProcessEngineConfiguration registerActivityType(Class< ? extends ActivityType> activityTypeClass) {
    super.registerActivityType(activityTypeClass);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerJavaBeanType(Class< ? > javaBeanClass) {
    super.registerJavaBeanType(javaBeanClass);
    return this;
  }

  @Override
  public MemoryProcessEngineConfiguration registerDataType(Class< ? extends DataType> dataTypeClass) {
    super.registerDataType(dataTypeClass);
    return this;
  }
}

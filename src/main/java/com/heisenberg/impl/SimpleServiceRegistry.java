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

import java.util.HashMap;
import java.util.Map;

import com.heisenberg.api.task.TaskService;
import com.heisenberg.impl.job.JobService;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.script.ScriptService;
import com.heisenberg.plugin.ServiceRegistry;


/**
 * @author Walter White
 */
public class SimpleServiceRegistry implements ServiceRegistry {
  
  Map<String,Object> services = new HashMap<>();
  Map<String, Class<?>> defaultServiceTypes = new HashMap<>();

  @SuppressWarnings("unchecked")
  public synchronized <T> T getService(Class<T> serviceInterface) {
    T service = (T) services.get(serviceInterface.getName());
    if (service==null) {
      service = defaultServiceTypes.get(serviceInterface.getName());
    }
    return service;
  }
  
  public JsonService getJsonService() {
    return getService(JsonService.class);
  }
  
  public ExecutorService getExecutorService() {
    return getService(ExecutorService.class);
  }
  
  public ScriptService getScriptService() {
    return getService(ScriptService.class);
  }
  
  public TaskService getTaskService() {
    return getService(TaskService.class);
  }
  
  public JobService getJobService() {
    return getService(JobService.class);
  }
}

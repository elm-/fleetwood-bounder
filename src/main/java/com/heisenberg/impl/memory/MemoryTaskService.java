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
package com.heisenberg.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.heisenberg.api.task.Task;
import com.heisenberg.api.task.TaskService;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.impl.task.TaskImpl;
import com.heisenberg.impl.task.TaskQueryImpl;
import com.heisenberg.impl.task.TaskServiceImpl;


/**
 * @author Walter White
 */
public class MemoryTaskService extends TaskServiceImpl implements TaskService {
  
  protected Map<Object, TaskImpl> tasks = Collections.synchronizedMap(new LinkedHashMap<Object,TaskImpl>());

  public MemoryTaskService() {
  }

  public MemoryTaskService(ServiceRegistry serviceRegistry) {
    this.tasks = Collections.synchronizedMap(new LinkedHashMap<Object,TaskImpl>());
  }

  @Override
  public void save(TaskImpl task) {
    TaskImpl t = (TaskImpl) task;
    t.setId(UUID.randomUUID().toString());
    tasks.put(t.getId(), t);
  }
  
  @Override
  public void deleteTask(String taskId) {
  }

  @Override
  public List<Task> findTasks(TaskQueryImpl taskQuery) {
    return new ArrayList<Task>(tasks.values());
  }
}

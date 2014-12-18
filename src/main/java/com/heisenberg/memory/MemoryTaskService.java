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
package com.heisenberg.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.heisenberg.api.task.Task;
import com.heisenberg.api.task.TaskService;


/**
 * @author Walter White
 */
public class MemoryTaskService implements TaskService {
  
  Map<Object, TaskImpl> tasks = Collections.synchronizedMap(new LinkedHashMap<Object,TaskImpl>());

  @Override
  public Task newTask() {
    return new TaskImpl();
  }

  @Override
  public void save(Task task) {
    TaskImpl t = (TaskImpl) task;
    t.id = UUID.randomUUID().toString();
    tasks.put(t.id, t);
  }

  public List<TaskImpl> getTasks() {
    return new ArrayList<>(tasks.values());
  }
}

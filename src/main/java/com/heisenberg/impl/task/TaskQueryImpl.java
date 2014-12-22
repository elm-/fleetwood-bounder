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
package com.heisenberg.impl.task;

import java.util.List;

import com.heisenberg.api.task.Task;
import com.heisenberg.api.task.TaskQuery;


/**
 * @author Walter White
 */
public class TaskQueryImpl implements TaskQuery {

  TaskServiceImpl taskService;
  
  public TaskQueryImpl(TaskServiceImpl taskService) {
    this.taskService = taskService;
  }

  @Override
  public List<Task> asList() {
    return taskService.findTasks(this);
  }

}

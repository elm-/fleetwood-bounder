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
package com.heisenberg.mongo;

import java.util.List;

import com.heisenberg.api.task.Task;
import com.heisenberg.api.task.TaskService;
import com.heisenberg.impl.task.TaskImpl;
import com.heisenberg.impl.task.TaskQueryImpl;
import com.heisenberg.impl.task.TaskServiceImpl;


/**
 * @author Walter White
 */
public class MongoTaskService extends TaskServiceImpl implements TaskService {

  @Override
  public void save(TaskImpl task) {
  }

  @Override
  public void deleteTask(String taskId) {
  }

  @Override
  public List<Task> findTasks(TaskQueryImpl taskQuery) {
    return null;
  }
}

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
package com.heisenberg.test.execution;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.engine.memory.MemoryTaskService;


/**
 * @author Walter White
 */
public class TaskTest {

  @Test
  public void testTask() throws Exception {
    MemoryProcessEngine processEngine = new MemoryProcessEngine();
    
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newActivity()
      .id("Task one")
      .activityType(new UserTask());
    
    String processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();
    
    MemoryTaskService taskService = processEngine.getTaskService();
    assertEquals("Task one", taskService.getTasks().get(0).getName());
  }
}
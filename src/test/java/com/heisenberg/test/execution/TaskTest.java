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

import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.memory.MemoryWorkflowEngine;
import com.heisenberg.memory.MemoryTaskService;


/**
 * @author Walter White
 */
public class TaskTest {

  @Test
  public void testTask() throws Exception {
    MemoryWorkflowEngine processEngine = new MemoryWorkflowEngine();
    
    WorkflowBuilder process = processEngine.newWorkflow();
    
    process.newActivity()
      .id("Task one")
      .activityType(new UserTask());
    
    String processDefinitionId = process
      .deploy()
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();
    
    processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();
    
    MemoryTaskService taskService = processEngine.getServiceRegistry().getService(MemoryTaskService.class);
    assertEquals("Task one", taskService.getTasks().get(0).getName());
  }
}

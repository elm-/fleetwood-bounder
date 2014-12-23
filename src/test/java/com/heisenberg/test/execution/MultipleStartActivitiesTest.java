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

import static com.heisenberg.test.TestHelper.assertOpen;
import static com.heisenberg.test.TestHelper.endTask;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.WorkflowTest;

/**
 * @author Walter White
 */
public class MultipleStartActivitiesTest extends WorkflowTest {
  
  @Test
  public void testDefaultStartActivitiesParallelExecution() {
    WorkflowBuilder w = workflowEngine.newWorkflow();

    w.newActivity()
      .activityType(new UserTask())
      .id("one");
    
    w.newActivity()
      .activityType(new UserTask())
      .id("two");
    
    w.newActivity()
      .activityType(new UserTask())
      .id("three");
    
    w.newTransition().from("two").to("three");
    
    String processDefinitionId = w.deploy()
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();
    
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();
    
    assertOpen(workflowInstance, "one", "two");
    
    workflowInstance = endTask(workflowEngine, workflowInstance, "two");

    assertOpen(workflowInstance, "one", "three");

    workflowInstance = endTask(workflowEngine, workflowInstance, "one");
    workflowInstance = endTask(workflowEngine, workflowInstance, "three");

    assertOpen(workflowInstance);

    assertTrue(workflowInstance.isEnded());
  }
}

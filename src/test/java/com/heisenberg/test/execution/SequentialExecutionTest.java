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
import static com.heisenberg.test.TestHelper.getActivityInstanceId;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.WorkflowTest;

/**
 * @author Walter White
 */
public class SequentialExecutionTest extends WorkflowTest {
  
  @Test
  public void testOne() {
    WorkflowBuilder process = workflowEngine.newWorkflow();

    process.newActivity()
      .activityType(new UserTask())
      .id("one");
    
    process.newActivity()
      .activityType(new UserTask())
      .id("two");
    
    process.newActivity()
      .activityType(new UserTask())
      .id("three");
    
    process.newTransition().from("one").to("two");
    process.newTransition().from("two").to("three");
    
    String processDefinitionId = process.deploy()
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();
    
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
      .startWorkflowInstance();
    
    assertOpen(workflowInstance, "one");
    
    String oneId = getActivityInstanceId(workflowInstance, "one");
    
    workflowInstance = workflowEngine.newMessage()
      .activityInstanceId(oneId)
      .send();

    assertOpen(workflowInstance, "two");
    
    String twoId = getActivityInstanceId(workflowInstance, "two");
    
    workflowInstance = workflowEngine.newMessage()
      .activityInstanceId(twoId)
      .send();


    assertOpen(workflowInstance, "three");
    
    String threeId = getActivityInstanceId(workflowInstance, "three");
    
    workflowInstance = workflowEngine.newMessage()
      .activityInstanceId(threeId)
      .send();

    assertTrue(workflowInstance.isEnded());
  }
}

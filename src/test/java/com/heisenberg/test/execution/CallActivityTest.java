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

import static com.heisenberg.test.TestHelper.findActivityInstanceOpen;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.heisenberg.api.activitytypes.CallActivity;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.WorkflowTest;

/**
 * @author Walter White
 */
public class CallActivityTest extends WorkflowTest {
  
  @Test
  public void testCallActivity() {
    WorkflowBuilder subWorkflow = workflowEngine.newWorkflow();
    subWorkflow.newActivity("subtask", new UserTask());
    String subprocessId = subWorkflow.deploy().getWorkflowId();
    
    WorkflowBuilder superWorkflow = workflowEngine.newWorkflow();
    superWorkflow.newActivity("call", new CallActivity().subProcessId(subprocessId));
    String superprocessId = superWorkflow.deploy().getWorkflowId();
    
    WorkflowInstance superInstance = workflowEngine.newStart()
      .workflowId(superprocessId)
      .startWorkflowInstance();
    
    ActivityInstance callActivityInstance = findActivityInstanceOpen(superInstance, "call");
    assertNotNull(callActivityInstance.getCalledWorkflowInstanceId());
    
    WorkflowInstance subInstance = workflowEngine.newWorkflowInstanceQuery()
      .workflowInstanceId(callActivityInstance.getCalledWorkflowInstanceId())
      .get();
    
    assertNotNull(subInstance);
    
    ActivityInstance subtaskInstance = findActivityInstanceOpen(subInstance, "subtask");
    
    subInstance = workflowEngine.newMessage()
      .processInstanceId(subInstance.getId())
      .activityInstanceId(subtaskInstance.getId())
      .send();
    assertTrue(subInstance.isEnded());

    superInstance = workflowEngine.newWorkflowInstanceQuery()
            .workflowInstanceId(superInstance.getId())
            .get();
    assertTrue(superInstance.isEnded());
  }
}

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

import com.heisenberg.api.activitytypes.EndEvent;
import com.heisenberg.api.activitytypes.ParallelGateway;
import com.heisenberg.api.activitytypes.StartEvent;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.WorkflowTest;

/**
 * @author Walter White
 */
public class ParallelGatewayTest extends WorkflowTest {
  
  @Test
  public void testParallelGateway() {
    /*                +-->[t1]------+
                      |             |
                 +-->[f2]           |
                 |    |             | 
     [start]-->[f1]   +-->[t2]-+   [j1]-->[end]
                 |             |    |
                 |            [j2]--+
                 |             |
                 +-->[t3]------+ 
    */

    WorkflowBuilder w = workflowEngine.newWorkflow();

    w.newActivity().activityType(new StartEvent()).id("start");
    w.newActivity().activityType(new ParallelGateway()).id("f1");
    w.newActivity().activityType(new ParallelGateway()).id("f2");
    w.newActivity().activityType(new UserTask()).id("t1");
    w.newActivity().activityType(new UserTask()).id("t2");
    w.newActivity().activityType(new UserTask()).id("t3");
    w.newActivity().activityType(new ParallelGateway()).id("j1");
    w.newActivity().activityType(new ParallelGateway()).id("j2");
    w.newActivity().activityType(new EndEvent()).id("end");

    w.newTransition().from("start").to("f1");
    w.newTransition().from("f1").to("f2");
    w.newTransition().from("f1").to("t3");
    w.newTransition().from("f2").to("t1");
    w.newTransition().from("f2").to("t2");
    w.newTransition().from("t1").to("j1");
    w.newTransition().from("t2").to("j2");
    w.newTransition().from("t3").to("j2");
    w.newTransition().from("j2").to("j1");
    w.newTransition().from("j1").to("end");

    String workflowId = w.deploy();
    
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(workflowId)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "t1", "t2", "t3");

    workflowInstance = endTask(workflowEngine, workflowInstance, "t1");

    assertOpen(workflowInstance, "t2", "t3");

    workflowInstance = endTask(workflowEngine, workflowInstance, "t2");

    assertOpen(workflowInstance, "t3");

    workflowInstance = endTask(workflowEngine, workflowInstance, "t3");

    assertOpen(workflowInstance);

    assertTrue(workflowInstance.isEnded());
  }
}

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

import org.junit.Test;

import com.heisenberg.api.DataTypes;
import com.heisenberg.api.activitytypes.ExclusiveGateway;
import com.heisenberg.api.activitytypes.StartEvent;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.WorkflowTest;

/**
 * @author Walter White
 */
public class ExclusiveGatewayTest extends WorkflowTest {
  
  @Test
  public void testExclusiveGateway() {
    WorkflowBuilder w = workflowEngine.newWorkflow();
    
    w.newVariable()
     .id("v")
     .dataType(DataTypes.NUMBER);

    w.newActivity().activityType(new StartEvent()).id("start");
    w.newActivity().activityType(new ExclusiveGateway())
      .id("?")
      .defaultTransition("default");
    
    w.newActivity().activityType(new UserTask()).id("t1");
    w.newActivity().activityType(new UserTask()).id("t2");
    w.newActivity().activityType(new UserTask()).id("t3");

    w.newTransition().from("start").to("?");
    w.newTransition().from("?").to("t1").condition("v < 10");
    w.newTransition().from("?").to("t2").condition("v < 100");
    w.newTransition().from("?").to("t3").id("default");

    String workflowId = w.deploy();
    
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(workflowId)
      .variableValue("v", 5)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "t1");

    workflowInstance = workflowEngine.newStart()
      .workflowId(workflowId)
      .variableValue("v", 50)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "t2");

    workflowInstance = workflowEngine.newStart()
      .workflowId(workflowId)
      .variableValue("v", 500)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "t3");
  }
}

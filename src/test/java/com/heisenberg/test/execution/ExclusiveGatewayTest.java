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
    WorkflowBuilder p = workflowEngine.newWorkflow();
    
    p.newVariable()
     .id("v")
     .dataType(DataTypes.NUMBER);

    p.newActivity().activityType(new StartEvent()).id("start");
    p.newActivity().activityType(new ExclusiveGateway())
      .id("?")
      .defaultTransition("default");
    
    p.newActivity().activityType(new UserTask()).id("t1");
    p.newActivity().activityType(new UserTask()).id("t2");
    p.newActivity().activityType(new UserTask()).id("t3");

    p.newTransition().from("start").to("?");
    p.newTransition().from("?").to("t1").condition("v < 10");
    p.newTransition().from("?").to("t2").condition("v < 100");
    p.newTransition().from("?").to("t3").id("default");

    String processDefinitionId = p.deploy()
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();
    
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
      .variableValue("v", 5)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "t1");

    workflowInstance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
      .variableValue("v", 50)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "t2");

    workflowInstance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
      .variableValue("v", 500)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "t3");
  }
}

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

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.ExclusiveGateway;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.memory.MemoryProcessEngine;
import com.heisenberg.plugin.DataTypes;

/**
 * @author Walter White
 */
public class ExclusiveGatewayTest {
  
  @Test
  public void testExclusiveGateway() {
    ProcessEngine processEngine = new MemoryProcessEngine();

    ProcessDefinitionBuilder p = processEngine.newProcessDefinition();
    
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
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", 5)
      .startProcessInstance();

    assertOpen(processInstance, "t1");

    processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", 50)
      .startProcessInstance();

    assertOpen(processInstance, "t2");

    processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", 500)
      .startProcessInstance();

    assertOpen(processInstance, "t3");
  }
}

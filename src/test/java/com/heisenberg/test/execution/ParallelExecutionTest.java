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
import static org.junit.Assert.*;

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.ParallelGateway;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;

/**
 * @author Walter White
 */
public class ParallelExecutionTest {
  
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
    
    ProcessEngine processEngine = new MemoryProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();

    process.newActivity().activityType(new StartEvent()).id("start");
    process.newActivity().activityType(new ParallelGateway()).id("f1");
    process.newActivity().activityType(new ParallelGateway()).id("f2");
    process.newActivity().activityType(new UserTask()).id("t1");
    process.newActivity().activityType(new UserTask()).id("t2");
    process.newActivity().activityType(new UserTask()).id("t3");
    process.newActivity().activityType(new ParallelGateway()).id("j1");
    process.newActivity().activityType(new ParallelGateway()).id("j2");
    process.newActivity().activityType(new EndEvent()).id("end");

    process.newTransition().from("start").to("f1");
    process.newTransition().from("f1").to("f2");
    process.newTransition().from("f1").to("t3");
    process.newTransition().from("f2").to("t1");
    process.newTransition().from("f2").to("t2");
    process.newTransition().from("t1").to("j1");
    process.newTransition().from("t2").to("j2");
    process.newTransition().from("t3").to("j2");
    process.newTransition().from("j2").to("j1");
    process.newTransition().from("j1").to("end");

    String processDefinitionId = process.deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();

    assertOpen(processInstance, "t1", "t2", "t3");

    processInstance = endTask(processEngine, processInstance, "t1");

    assertOpen(processInstance, "t2", "t3");

    processInstance = endTask(processEngine, processInstance, "t2");

    assertOpen(processInstance, "t3");

    processInstance = endTask(processEngine, processInstance, "t3");

    assertOpen(processInstance);

    assertTrue(processInstance.isEnded());
  }
}

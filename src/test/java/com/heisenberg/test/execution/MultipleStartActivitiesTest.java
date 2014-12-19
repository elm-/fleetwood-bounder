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

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.memory.MemoryProcessEngine;

/**
 * @author Walter White
 */
public class MultipleStartActivitiesTest {
  
  @Test
  public void testDefaultStartActivitiesParallelExecution() {
    ProcessEngine processEngine = new MemoryProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();

    process.newActivity()
      .activityType(new UserTask())
      .id("one");
    
    process.newActivity()
      .activityType(new UserTask())
      .id("two");
    
    process.newActivity()
      .activityType(new UserTask())
      .id("three");
    
    process.newTransition().from("two").to("three");
    
    String processDefinitionId = process.deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();
    
    assertOpen(processInstance, "one", "two");
    
    processInstance = endTask(processEngine, processInstance, "two");

    assertOpen(processInstance, "one", "three");

    processInstance = endTask(processEngine, processInstance, "one");
    processInstance = endTask(processEngine, processInstance, "three");

    assertOpen(processInstance);

    assertTrue(processInstance.isEnded());
  }
}

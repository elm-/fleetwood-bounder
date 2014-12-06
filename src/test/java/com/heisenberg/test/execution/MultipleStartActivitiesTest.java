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
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.ScriptTask;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.test.TestHelper;

/**
 * @author Walter White
 */
public class MultipleStartActivitiesTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();

    process.newActivity()
      .activityType(new ScriptTask())
      .id("automatic");
    
    process.newActivity()
      .activityType(new UserTask())
      .id("wait1");
    
    process.newActivity()
      .activityType(new UserTask())
      .id("wait2");
    
    process.newTransition()
      .from("wait1")
      .to("wait2");
    
    String processDefinitionId = process.deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();

    assertNotNull(processInstance.getId());
    TestHelper.assertOpen(processInstance, "wait1");
    assertEquals("Expected 2 but was "+processInstance.getActivityInstances(), 2, processInstance.getActivityInstances().size());
  }
}

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.type.TextType;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.test.db.MongoProcessEngineTest.Go;

/**
 * @author Walter White
 */
public class MultipleStartActivitiesTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerConfigurableActivityType(new Go())
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();

    process.newVariable()
      .id("t")
      .dataType(TextType.INSTANCE);

    Go go = new Go()
      .placeBinding(new Binding<String>().expression("t.toLowerCase()"));
    
    process.newActivity()
      .activityType(go)
      .id("go");
    
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
      .variableValue("t", "Hello World")
      .startProcessInstance();

    assertNotNull(processInstance.getId());
    assertEquals("Expected 2 but was "+processInstance.getActivityInstances(), 2, processInstance.getActivityInstances().size());

    assertEquals(1, go.executions.size());
    Go.Execution goExecution = go.executions.get(0);
    ActivityDefinitionImpl activityDefinition = (ActivityDefinitionImpl) goExecution.activityInstance.getActivityDefinition();
    assertEquals(Go.class, activityDefinition.activityType.getClass());
    assertEquals("hello world", goExecution.place);
    
    ActivityInstance goInstance = processInstance.getActivityInstances().get(0);
    assertTrue(goInstance.isEnded());
    assertEquals("go", goInstance.getActivityDefinitionId());
    
    ActivityInstance waitActivityInstance = processInstance.getActivityInstances().get(1);
    assertFalse(waitActivityInstance.isEnded());
    assertEquals("wait1", waitActivityInstance.getActivityDefinitionId());
    
    // signalRequest.putVariable(variableDefinition.getId(), "hello world2");
    processInstance = processEngine.newMessage()
      .activityInstanceId(waitActivityInstance.getId())
      .send();
    
    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());

    goInstance = processInstance.getActivityInstances().get(0);
    assertTrue(goInstance.isEnded());
    assertEquals("go", goInstance.getActivityDefinitionId());
    
    waitActivityInstance = processInstance.getActivityInstances().get(1);
    assertTrue(waitActivityInstance.isEnded());
    assertEquals("wait1", waitActivityInstance.getActivityDefinitionId());
    
    ActivityInstance wait2ActivityInstance = processInstance.getActivityInstances().get(2);
    assertFalse(wait2ActivityInstance.isEnded());
    assertEquals("wait2", wait2ActivityInstance.getActivityDefinitionId());

    processInstance = processEngine.newMessage()
      .activityInstanceId(wait2ActivityInstance.getId())
      .send();
    
    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());
  }
}

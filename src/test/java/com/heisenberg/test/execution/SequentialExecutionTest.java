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

import com.heisenberg.api.ActivityInstanceMessageBuilder;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.ProcessInstanceBuilder;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.type.TextType;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.test.Go;
import com.heisenberg.test.Wait;
import com.heisenberg.test.Go.Execution;

/**
 * @author Walter White
 */
public class SequentialExecutionTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class)
      .registerActivityType(Wait.class);

    ProcessDefinitionBuilder processBuilder = processEngine.newProcessDefinition();

    processBuilder.newVariable()
      .id("t")
      .dataType(TextType.INSTANCE);

    Go go = new Go()
      .placeBinding(new Binding<String>().expression("t.toLowerCase()"));
    
    processBuilder.newActivity()
      .activityType(go)
      .id("go");
    
    processBuilder.newActivity()
      .activityType(new UserTask())
      .id("wait1");
    
    processBuilder.newActivity()
      .activityType(new UserTask())
      .id("wait2");
    
    processBuilder.newTransition()
      .from("wait1")
      .to("wait2");
    
    String processDefinitionId = processEngine
      .deployProcessDefinition(processBuilder)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.newProcessInstance()
      .processDefinitionId(processDefinitionId)
      .variableValue("t", "Hello World"));

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
    processInstance = processEngine.sendActivityInstanceMessage(new ActivityInstanceMessageBuilder()
      .activityInstanceId(waitActivityInstance.getId()));
    
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

    processInstance = processEngine.sendActivityInstanceMessage(new ActivityInstanceMessageBuilder()
      .activityInstanceId(wait2ActivityInstance.getId()));
    
    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());
  }
}

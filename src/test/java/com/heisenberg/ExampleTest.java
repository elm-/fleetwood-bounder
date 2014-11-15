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
package com.heisenberg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.definition.VariableDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.spi.Type;

/**
 * @author Walter White
 */
public class ExampleTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(new Go())
      .registerActivityType(new Wait())
      .registerType(Type.TEXT);

    // prepare the ingredients
    VariableDefinition t = new VariableDefinition()
      .type(Type.TEXT)
      .name("t");
    
    ActivityDefinition go = new ActivityDefinition()
      .type(Go.ID)
      .parameterValue(Go.PLACE, "Antwerp")
      .name("go");
    
    ActivityDefinition wait1 = new ActivityDefinition()
      .type(Wait.ID)
      .name("wait1");
    
    ActivityDefinition wait2 = new ActivityDefinition()
      .type(Wait.ID)
      .name("wait2");
    
    // cook the process
    ProcessDefinition processDefinition = new ProcessDefinition()
      .activity(go)
      .activity(wait1)
      .activity(wait2)
      .transition(wait1, wait2)
      .variable(t);

    String processDefinitionId = processEngine
      .deployProcessDefinition(processDefinition)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("t", "hello world"));

    assertNotNull(processInstance.id);
    assertEquals("Expected 2 but was "+processInstance.activityInstances, 2, processInstance.activityInstances.size());

    assertEquals(1, Go.executions.size());
    Go.Execution goExecution = Go.executions.get(0);
    assertEquals(Go.class, goExecution.activityInstance.activityDefinition.activityType.getClass());
    assertEquals("Antwerp", goExecution.place);
    
//    ActivityInstanceImpl goInstance = processInstance.getActivityInstances().get(0);
//    assertTrue(goInstance.isEnded());
//    assertEquals(go, goInstance.getActivityDefinition());
//    
//    ActivityInstanceImpl waitActivityInstance = processInstance.getActivityInstances().get(1);
//    assertFalse(waitActivityInstance.isEnded());
//    assertEquals(wait, waitActivityInstance.getActivityDefinition());
//    
//    // signalRequest.putVariable(variableDefinition.getId(), "hello world2");
//    processInstance = processEngine.signal(new SignalRequest()
//      .activityInstanceId(waitActivityInstance.getId()));
//    
//    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());
//
//    goInstance = processInstance.getActivityInstances().get(0);
//    assertTrue(goInstance.isEnded());
//    assertEquals(go, goInstance.getActivityDefinition());
//    
//    waitActivityInstance = processInstance.getActivityInstances().get(1);
//    assertTrue(waitActivityInstance.isEnded());
//    assertEquals(wait, waitActivityInstance.getActivityDefinition());
//    
//    ActivityInstanceImpl wait2ActivityInstance = processInstance.getActivityInstances().get(2);
//    assertFalse(wait2ActivityInstance.isEnded());
//    assertEquals(wait2, wait2ActivityInstance.getActivityDefinition());
//
//    processInstance = processEngine.signal(new SignalRequest()
//      .activityInstanceId(wait2ActivityInstance.getId()));
//    
//    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());
  }
}

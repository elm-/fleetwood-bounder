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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.heisenberg.Go.Execution;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.definition.VariableDefinition;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.ProcessInstanceId;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.spi.Type;

/**
 * @author Walter White
 */
public class ExampleTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine();

    // prepare the ingredients
    VariableDefinition t = VariableDefinition
      .type(Type.TEXT);
    
    ActivityDefinition go = ActivityDefinition
      .activityType(Go.ID)
      .parameterValue(Go.PLACE, "Antwerp");
    
    ActivityDefinition wait = ActivityDefinition
      .activityType(Wait.ID);
    
    ActivityDefinition wait2 = ActivityDefinition
      .activityType(Wait.ID);
    
    // cook the process
    ProcessDefinition processDefinition = new ProcessDefinition()
      .activity(go)
      .activity(wait)
      .activity(wait2)
      .transition(wait, wait2)
      .variable(t);

    processDefinition = processEngine.saveProcessDefinition(processDefinition);
    
    ProcessInstanceImpl processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinition.getId())
      .variableValue(t.getId(), "hello world"));
    
    assertEquals(1, Go.executions.size());
    Go.Execution goExecution = Go.executions.get(0);
    assertEquals(go, goExecution.activityInstance.getActivityDefinition());
    assertEquals("Antwerp", goExecution.place);
    
    ProcessInstanceId processInstanceId = processInstance.getId();
    assertNotNull(processInstanceId);
    assertEquals("Expected 2 but was "+processInstance.getActivityInstances(), 2, processInstance.getActivityInstances().size());
    
    ActivityInstanceImpl goInstance = processInstance.getActivityInstances().get(0);
    assertTrue(goInstance.isEnded());
    assertEquals(go, goInstance.getActivityDefinition());
    
    ActivityInstanceImpl waitActivityInstance = processInstance.getActivityInstances().get(1);
    assertFalse(waitActivityInstance.isEnded());
    assertEquals(wait, waitActivityInstance.getActivityDefinition());
    
    // signalRequest.putVariable(variableDefinition.getId(), "hello world2");
    processInstance = processEngine.signal(new SignalRequest()
      .activityInstanceId(waitActivityInstance.getId()));
    
    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());

    goInstance = processInstance.getActivityInstances().get(0);
    assertTrue(goInstance.isEnded());
    assertEquals(go, goInstance.getActivityDefinition());
    
    waitActivityInstance = processInstance.getActivityInstances().get(1);
    assertTrue(waitActivityInstance.isEnded());
    assertEquals(wait, waitActivityInstance.getActivityDefinition());
    
    ActivityInstanceImpl wait2ActivityInstance = processInstance.getActivityInstances().get(2);
    assertFalse(wait2ActivityInstance.isEnded());
    assertEquals(wait2, wait2ActivityInstance.getActivityDefinition());

    processInstance = processEngine.signal(new SignalRequest()
      .activityInstanceId(wait2ActivityInstance.getId()));
    
    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());
  }
}

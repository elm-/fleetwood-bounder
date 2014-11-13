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

import com.heisenberg.definition.ActivityDefinition;
import com.heisenberg.definition.ProcessDefinition;
import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.definition.VariableDefinition;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.instance.ActivityInstance;
import com.heisenberg.instance.ProcessInstance;
import com.heisenberg.instance.ProcessInstanceId;
import com.heisenberg.type.Type;

/**
 * @author Walter White
 */
public class ExampleTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine();

    // prepare the ingredients
    VariableDefinition t = new VariableDefinition()
      .type(Type.TEXT);
    
    ActivityDefinition go = new Go();
    ActivityDefinition wait = new Wait();
    ActivityDefinition wait2 = new Wait();
    
    // cook a process batch
    ProcessDefinition processDefinition = new ProcessDefinition()
      .activity(go)
      .activity(wait)
      .activity(wait2)
      .transition(wait, wait2)
      .variable(t);

    processDefinition = processEngine.saveProcessDefinition(processDefinition);
    ProcessDefinitionId processDefinitionId = processDefinition.getId();
    
    CreateProcessInstanceRequest createProcessInstanceRequest = new CreateProcessInstanceRequest();
    createProcessInstanceRequest.setProcessDefinitionId(processDefinitionId);
    createProcessInstanceRequest.variableValue(t.getId(), "hello world");
    ProcessInstance processInstance = processEngine.createProcessInstance(createProcessInstanceRequest);
    
    ProcessInstanceId processInstanceId = processInstance.getId();
    assertNotNull(processInstanceId);
    assertEquals("Expected 2 but was "+processInstance.getActivityInstances(), 2, processInstance.getActivityInstances().size());
    
    ActivityInstance goInstance = processInstance.getActivityInstances().get(0);
    assertTrue(goInstance.isEnded());
    assertEquals(go, goInstance.getActivityDefinition());
    
    ActivityInstance waitActivityInstance = processInstance.getActivityInstances().get(1);
    assertFalse(waitActivityInstance.isEnded());
    assertEquals(wait, waitActivityInstance.getActivityDefinition());
    
    SignalRequest signalRequest = new SignalRequest();
    signalRequest.setActivityInstanceId(waitActivityInstance.getId());
    // signalRequest.putVariable(variableDefinition.getId(), "hello world2");
    processInstance = processEngine.signal(signalRequest);
    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());

    goInstance = processInstance.getActivityInstances().get(0);
    assertTrue(goInstance.isEnded());
    assertEquals(go, goInstance.getActivityDefinition());
    
    waitActivityInstance = processInstance.getActivityInstances().get(1);
    assertTrue(waitActivityInstance.isEnded());
    assertEquals(wait, waitActivityInstance.getActivityDefinition());
    
    ActivityInstance wait2ActivityInstance = processInstance.getActivityInstances().get(2);
    assertFalse(wait2ActivityInstance.isEnded());
    assertEquals(wait2, wait2ActivityInstance.getActivityDefinition());

    signalRequest = new SignalRequest();
    signalRequest.setActivityInstanceId(wait2ActivityInstance.getId());
    processInstance = processEngine.signal(signalRequest);
    assertEquals("Expected 3 but was "+processInstance.getActivityInstances(), 3, processInstance.getActivityInstances().size());
  }
}

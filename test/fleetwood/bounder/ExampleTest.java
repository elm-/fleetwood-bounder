/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.engine.memory.MemoryProcessEngine;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.types.TextVariableDefinition;

/**
 * @author Walter White
 */
public class ExampleTest {
  
  @Test
  public void testOne() {
    // TODO
    // ensure jackson lib is not required if json is not used
    // static process variables
    // transient execution context variables

    ProcessDefinition processDefinition = new ProcessDefinition();
    
    Go go = new Go();
    processDefinition.addActivityDefinition(go);
    Wait wait = new Wait();
    processDefinition.addActivityDefinition(wait);
    Wait wait2 = new Wait();
    processDefinition.addActivityDefinition(wait2);
    
    processDefinition.addTransitionDefinition(wait, wait2);

    VariableDefinition variableDefinition = new TextVariableDefinition();
    variableDefinition.setName("v");
    processDefinition.addVariableDefinition(variableDefinition);

    ProcessEngine processEngine = new MemoryProcessEngine();
    processDefinition = processEngine.saveProcessDefinition(processDefinition);
    ProcessDefinitionId processDefinitionId = processDefinition.getId();
    
    CreateProcessInstanceRequest createProcessInstanceRequest = new CreateProcessInstanceRequest();
    createProcessInstanceRequest.setProcessDefinitionId(processDefinitionId);
    // createProcessInstanceRequest.putVariable(variableDefinition.getId(), "hello world");
    ProcessInstance processInstance = processEngine.createProcessInstance(createProcessInstanceRequest);
    
    ProcessInstanceId processInstanceId = processInstance.getId();
    assertNotNull(processInstanceId);
    assertEquals("Expected 2 but was "+processInstance.getActivityInstances(), 2, processInstance.getActivityInstances().size());
    
    assertEquals(1, go.activityInstances.size());
    assertTrue(go.activityInstances.get(0).isEnded());
    assertEquals(1, wait.activityInstances.size());
    ActivityInstance waitActivityInstance = wait.activityInstances.get(0);
    assertFalse(waitActivityInstance.isEnded());
    
    SignalRequest signalRequest = new SignalRequest();
    signalRequest.setActivityInstanceId(waitActivityInstance.getId());
    // signalRequest.putVariable(variableDefinition.getId(), "hello world2");
    processInstance = processEngine.signal(signalRequest);
  }
}

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

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableType;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.store.memory.MemoryProcessStore;

/**
 * @author Tom Baeyens
 */
public class ProcessExecutionTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new ProcessEngine();
    processEngine.setProcessStore(new MemoryProcessStore());
    
    ProcessDefinition processDefinition = processEngine.createNewProcessDefinition();
    Go go = new Go();
    processDefinition.addActivityDefinition(go);
    Wait wait = new Wait();
    processDefinition.addActivityDefinition(wait);

    VariableDefinition variable = new VariableDefinition();
    variable.setName("v");
    variable.setType(VariableType.TEXT);
    processDefinition.addVariable(variable);

    ProcessDefinitionId processDefinitionId = processEngine.saveProcessDefinition(processDefinition);
    
    ProcessInstance newProcessInstance = processEngine.createNewProcessInstance(processDefinitionId);
    newProcessInstance.setVariableByName("v", new Object());
    newProcessInstance.start();
    newProcessInstance.save();
    ProcessInstanceId processInstanceId = newProcessInstance.getId();
    assertNotNull(processInstanceId);
    
    assertEquals(1, go.activityInstances.size());
    assertTrue(go.activityInstances.get(0).isEnded());
    assertEquals(1, wait.activityInstances.size());
    ActivityInstance waitActivityInstance = wait.activityInstances.get(0);
    assertFalse(waitActivityInstance.isEnded());
    
    ActivityInstanceId waitActivityInstanceId = waitActivityInstance.getId();
    ProcessInstance processInstance = processEngine.createProcessInstanceQuery()
      .activityInstanceId(waitActivityInstanceId)
      .lock();
    
    ActivityInstance activityInstance = processInstance.findActivityInstance(waitActivityInstanceId);
    activityInstance.setVariableByName("v", new Object());
    activityInstance.signal();
    
    processInstance.save();
  }
  
  // static process variables
  // transient execution context variables
}

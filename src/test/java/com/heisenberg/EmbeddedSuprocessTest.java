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

import static com.heisenberg.TestHelper.assertOpenActivityInstances;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.SignalRequest;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.id.ActivityInstanceId;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.bpmn.activities.EmbeddedSubprocess;
import com.heisenberg.engine.memory.MemoryProcessEngine;


/**
 * @author Walter White
 */
public class EmbeddedSuprocessTest {
  
  @Test 
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class)
      .registerActivityType(Wait.class);
  
    ProcessBuilder process = processEngine.newProcess();
  
    process.newActivity()
      .activityType(new Go())
      .name("before");
    
    ActivityBuilder subprocess = process.newActivity()
      .activityType(EmbeddedSubprocess.INSTANCE)
      .name("subprocess");
    
    subprocess.newActivity()
      .activityType(Wait.INSTANCE)
      .name("w1");
  
    subprocess.newActivity()
      .activityType(Wait.INSTANCE)
      .name("w2");
  
    process.newActivity()
      .activityType(Wait.INSTANCE)
      .name("after");
  
    process.newTransition()
      .from("before")
      .to("subprocess");
    
    process.newTransition()
      .from("subprocess")
      .to("after");
  
    String processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId));

    assertOpenActivityInstances(processInstance, "subprocess", "w1", "w2");
    
    ActivityInstanceId w1Id = processInstance.findActivityInstanceByName("w1").getId();
    assertNotNull(w1Id);
    
    processEngine.signal(new SignalRequest()
      .activityInstanceId(w1Id));
  }
}

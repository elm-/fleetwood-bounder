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
package com.heisenberg.test;

import static com.heisenberg.test.TestHelper.assertActivityInstancesOpen;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.SignalRequest;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.activities.bpmn.EmbeddedSubprocess;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.util.ActivityInstanceId;
import com.heisenberg.api.util.ProcessDefinitionId;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;


/**
 * @author Walter White
 */
public class EmbeddedSuprocessTest {
  
  /**          +-------------+
   *           | sub         |
   * +-----+   | +--+   +--+ |   +---+
   * |start|-->| |w1|-->|w2| |-->|end|
   * +-----+   | +--+   +--+ |   +---+
   *           +-------------+
   */ 
  @Test public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class)
      .registerActivityType(Wait.class);
  
    ProcessBuilder process = processEngine.newProcess();
  
    process.newActivity()
      .activityType(new Go())
      .name("start");
    
    ActivityBuilder subprocess = process.newActivity()
      .activityType(EmbeddedSubprocess.INSTANCE)
      .name("sub");
    
    subprocess.newActivity()
      .activityType(Wait.INSTANCE)
      .name("w1");
  
    subprocess.newActivity()
      .activityType(Wait.INSTANCE)
      .name("w2");
  
    process.newActivity()
      .activityType(Wait.INSTANCE)
      .name("end");
  
    process.newTransition()
      .from("start")
      .to("sub");
    
    process.newTransition()
      .from("sub")
      .to("end");
  
    ProcessDefinitionId processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinitionId));

    assertActivityInstancesOpen(processInstance, "sub", "w1", "w2");
    
    ActivityInstanceId w1Id = processInstance.findActivityInstanceByName("w1").getId();
    assertNotNull(w1Id);
    
    processEngine.signal(new SignalRequest()
      .activityInstanceId(w1Id));
    

  }
}
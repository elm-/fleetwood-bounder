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

import static com.heisenberg.test.TestHelper.assertOpen;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.EmbeddedSubprocess;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.ScriptTask;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
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
    ProcessEngine processEngine = new MemoryProcessEngine();
  
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
  
    process.newActivity()
      .activityType(new ScriptTask())
      .id("start");
    
    ActivityBuilder subprocess = process.newActivity()
      .activityType(EmbeddedSubprocess.INSTANCE)
      .id("sub");
    
    subprocess.newActivity()
      .activityType(new UserTask())
      .id("w1");
  
    subprocess.newActivity()
      .activityType(new UserTask())
      .id("w2");
  
    process.newActivity()
      .activityType(new EndEvent())
      .id("end");
  
    process.newTransition()
      .from("start")
      .to("sub");
    
    process.newTransition()
      .from("sub")
      .to("end");
  
    String processDefinitionId = process
      .deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine
      .newTrigger()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();

    assertOpen(processInstance, "sub", "w1", "w2");
    
    String w1Id = processInstance.findActivityInstanceByActivityDefinitionId("w1").getId();
    assertNotNull(w1Id);
    
    processEngine.newMessage()
      .activityInstanceId(w1Id)
      .send();
  }
}

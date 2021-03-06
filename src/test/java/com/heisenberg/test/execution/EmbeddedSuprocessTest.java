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
import static com.heisenberg.test.TestHelper.endTask;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.heisenberg.api.activitytypes.EmbeddedSubprocess;
import com.heisenberg.api.activitytypes.EndEvent;
import com.heisenberg.api.activitytypes.ScriptTask;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.WorkflowTest;


/**
 * @author Walter White
 */
public class EmbeddedSuprocessTest extends WorkflowTest {
  
  /**          +-------------+
   *           | sub         |
   * +-----+   | +--+   +--+ |   +---+
   * |start|-->| |w1|   |w2| |-->|end|
   * +-----+   | +--+   +--+ |   +---+
   *           +-------------+
   */ 
  @Test 
  public void testOne() {
    WorkflowBuilder process = workflowEngine.newWorkflow();
  
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
      .getWorkflowId();
    
    WorkflowInstance workflowInstance = workflowEngine
      .newStart()
      .workflowId(processDefinitionId)
      .startWorkflowInstance();

    assertOpen(workflowInstance, "sub", "w1", "w2");
    
    workflowInstance = endTask(workflowEngine, workflowInstance, "w1");

    assertOpen(workflowInstance, "sub", "w2");

    workflowInstance = endTask(workflowEngine, workflowInstance, "w2");
    
    assertTrue(workflowInstance.isEnded());
  }
}

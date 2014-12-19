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

import org.junit.Test;

import com.heisenberg.api.DataTypes;
import com.heisenberg.api.activitytypes.ScriptTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.WorkflowTest;


/**
 * @author Walter White
 */
public class EventListenerTest extends WorkflowTest {
  
  @Test
  public void testScript() {
    WorkflowBuilder process = workflowEngine.newWorkflow();
    
    process.newVariable()
      .id("n")
      .dataType(DataTypes.TEXT);
  
    process.newVariable()
      .id("m")
      .dataType(DataTypes.TEXT);
  
    process.newActivity()
      .activityType(new ScriptTask()
        .variableMapping("name", "n")
        .variableMapping("message", "m")
        .script("message = 'Hello ' + name;")
      )
      .id("a");

    String processDefinitionId = process
      .deploy()
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();
    
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
      .variableValue("n", "World")
      .startWorkflowInstance();

    assertEquals("Hello World", workflowInstance.getVariableValue("m"));
  }
}

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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.definition.ProcessDefinition;
import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.definition.VariableDefinition;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.expressions.JavaScript;
import com.heisenberg.expressions.ScriptInput;
import com.heisenberg.expressions.ScriptOutput;
import com.heisenberg.instance.ProcessInstance;
import com.heisenberg.type.Type;


/**
 * @author Walter White
 */
public class ScriptTest {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine();

    // prepare the ingredients
    VariableDefinition t = new VariableDefinition()
      .type(Type.TEXT);
    
    // cook a process batch
    ProcessDefinition processDefinition = new ProcessDefinition()
      .variable(t);

    processDefinition = processEngine.saveProcessDefinition(processDefinition);
    ProcessDefinitionId processDefinitionId = processDefinition.getId();
    
    CreateProcessInstanceRequest createProcessInstanceRequest = new CreateProcessInstanceRequest();
    createProcessInstanceRequest.setProcessDefinitionId(processDefinitionId);
    createProcessInstanceRequest.variableValue(t.getId(), "hello world");
    ProcessInstance processInstance = processEngine.createProcessInstance(createProcessInstanceRequest);

    JavaScript javaScript = new JavaScript();
    ScriptInput scriptInput = new ScriptInput()
     .scopeInstance(processInstance)
     .scriptVariableBinding("message", t.getId())
     .script("'Hi, '+v0;");
    ScriptOutput scriptOutput = javaScript.evaluateScript(scriptInput);
    assertEquals("Hi, hello world", scriptOutput.getResult());
  }
  
  public static void myFunction(String message) {
    log.debug(message);
  }
}

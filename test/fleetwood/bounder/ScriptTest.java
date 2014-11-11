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

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.engine.memory.MemoryProcessEngine;
import fleetwood.bounder.expressions.ScriptEvaluatorImpl;
import fleetwood.bounder.expressions.ScriptInput;
import fleetwood.bounder.expressions.ScriptOutput;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.type.TextValue;
import fleetwood.bounder.type.Type;


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
    createProcessInstanceRequest.variableValue(t.getId(), new TextValue("hello world"));
    ProcessInstance processInstance = processEngine.createProcessInstance(createProcessInstanceRequest);

    ScriptEvaluatorImpl scriptEvaluator = new ScriptEvaluatorImpl();
    ScriptInput scriptInput = new ScriptInput()
     .scopeInstance(processInstance)
     .inputVariableName(t.getId(), "message")
     .outputVariableName("body")
     .script("'Hi, '+message;");
    ScriptOutput scriptOutput = scriptEvaluator.evaluateScript(scriptInput);
    assertEquals("Hi, hello world", scriptOutput.getResult());
  }
  
  public static void myFunction(String message) {
    log.debug(message);
  }
}

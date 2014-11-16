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

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.definition.VariableDefinition;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.expressions.Script;
import com.heisenberg.expressions.ScriptResult;
import com.heisenberg.expressions.Scripts;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class ScriptTest {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);
  
  // TODO Test if the script engine is thread safe.
  //      CompiledScript seems to be tied to a ScriptEngine.
  //      It should be investigated if concurrent script execution can overwrite each other's context.

  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(new ScriptActivity())
      .registerType(Type.TEXT);

    // prepare the ingredients
    VariableDefinition t = new VariableDefinition()
      .name("t")
      .type(Type.TEXT);
    
    ActivityDefinition a = new ActivityDefinition()
      .type("script")
      .name("a");

    // cook a process batch
    ProcessDefinition processDefinition = new ProcessDefinition()
      .variable(t)
      .activity(a);

    String processDefinitionId = processEngine
      .deployProcessDefinition(processDefinition)
      .checkNoErrorsAndNoWarnings()
      .processDefinitionId;
    
    processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("t", "hello world"));

    assertEquals("Hi, hello world", scriptResultMessage);
    
  }
  
  String scriptResultMessage = null;
  
  public class ScriptActivity extends ActivityType {
    @Override
    public String getId() {
      return "script";
    }
    @Override
    public void start(ActivityInstanceImpl activityInstance) {
      Scripts scripts = activityInstance.processEngine.scripts;
      Script script = scripts.compile("'Hi, '+message;")
       .scriptToProcessMapping("message", "t");
      ScriptResult scriptOutput = scripts.evaluateScript(activityInstance, script);
      scriptResultMessage = (String) scriptOutput.getResult();
    }
  }
  
  public static void myFunction(String message) {
    log.debug(message);
  }
}

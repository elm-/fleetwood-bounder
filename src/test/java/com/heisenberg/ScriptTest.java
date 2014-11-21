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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.expressions.Script;
import com.heisenberg.expressions.ScriptResult;
import com.heisenberg.expressions.ScriptRunner;
import com.heisenberg.spi.AbstractActivityType;
import com.heisenberg.spi.ControllableActivityInstance;


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
      .registerJavaBeanType(Money.class);

    ProcessBuilder processBuilder = processEngine.newProcess();
    
    processBuilder.newVariable()
      .name("m")
      .dataTypeJavaBean(Money.class);
    
    processBuilder.newActivity()
      .activityType(new ScriptActivity())
      .name("a");

    String processDefinitionId = processEngine
      .deployProcessDefinition(processBuilder)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    Map<String,Object> fiveDollars = new HashMap<>();
    fiveDollars.put("amount", 5d);
    fiveDollars.put("currency", "USD");
    
    processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("m", new Money(5d, "USD")));

    assertEquals("It costs 5, which is in USD\nAnd mmmoney is 5.0 USD", scriptResultMessage);
  }
  
  String scriptResultMessage = null;

  @JsonTypeName("script")
  public class ScriptActivity extends AbstractActivityType {
    @Override
    public String getLabel() {
      return "Script";
    }
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      ScriptRunner scriptRunner = activityInstance.getScriptRunner();
      Script script = scriptRunner.compile(
             // Each variable is automatically available with it's variableDefinitionName
             // Individual fields (and on Rhino also properties) can be dereferenced
             "'It costs '+m.amount+', which is in '+m.currency+'\\n"
             // Script to process variable mappings can be defined
             // The toString of the money java object will be used 
             +"And mmmoney is '+mmmoney")
        .scriptToProcessMapping("mmmoney", "m");
      ScriptResult scriptResult = scriptRunner.evaluateScript(activityInstance, script);
      scriptResultMessage = (String) scriptResult.getResult();
      activityInstance.onwards();
    }
  }
}

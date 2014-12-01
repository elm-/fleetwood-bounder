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
package com.heisenberg.examples;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.script.Script;
import com.heisenberg.impl.script.ScriptResult;
import com.heisenberg.impl.script.ScriptService;


/**
 * @author Walter White
 */
public class ScriptTest {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerJavaBeanType(Money.class)
      .registerActivityType(new ScriptActivity());

    ProcessBuilder processBuilder = processEngine.newProcess();
    
    processBuilder.newVariable()
      .id("m")
      .dataTypeJavaBean(Money.class);
    
    processBuilder.newActivity()
      .activityType(new ScriptActivity())
      .id("a");

    String processDefinitionId = processEngine
      .deployProcessDefinition(processBuilder)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    Map<String,Object> fiveDollars = new HashMap<>();
    fiveDollars.put("amount", 5d);
    fiveDollars.put("currency", "USD");
    
    processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinitionId)
      .variableValue("m", new Money(5d, "USD")));

    assertEquals("It costs 5, which is in USD\nAnd mmmoney is 5.0 USD", scriptResultMessage);
  }
  
  String scriptResultMessage = null;

  public class ScriptActivity extends AbstractActivityType {
    @Override
    public String getLabel() {
      return "Script";
    }
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      ScriptService scriptService = activityInstance.getScriptService();
      Script script = scriptService.compile(
             // Each variable is automatically available with it's variableDefinitionName
             // Individual fields (and on Rhino also properties) can be dereferenced
             "'It costs '+m.amount+', which is in '+m.currency+'\\n"
             // Script to process variable mappings can be defined
             // The toString of the money java object will be used 
             +"And mmmoney is '+mmmoney")
        .scriptToProcessMapping("mmmoney", "m");
      ScriptResult scriptResult = scriptService.evaluateScript(activityInstance, script);
      scriptResultMessage = (String) scriptResult.getResult();
      activityInstance.onwards();
    }
    @Override
    public String getTypeId() {
      return "testScript";
    }
  }
}

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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.activities.Label;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.configuration.Script;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.impl.script.ScriptResult;


/**
 * @author Walter White
 */
public class ScriptTest {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerJavaBeanType(Money.class)
      .registerSingletonActivityType(new ScriptActivity())
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("m")
      .dataTypeJavaBean(Money.class);
    
    process.newActivity()
      .activityType(new ScriptActivity())
      .id("a");

    String processDefinitionId = process
      .deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    Map<String,Object> fiveDollars = new HashMap<>();
    fiveDollars.put("amount", 5d);
    fiveDollars.put("currency", "USD");
    
    processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .variableValue("m", new Money(5d, "USD"))
      .startProcessInstance();

    assertEquals("It costs 5, which is in USD\nAnd mmmoney is 5.0 USD", scriptResultMessage);
  }
  
  static String scriptResultMessage = null;

  @JsonTypeName("testScript")
  @Label("Script")
  public static class ScriptActivity extends AbstractActivityType {
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
  }
  
  public static class Money {

    public double amount;
    public String currency;

    public Money(double amount, String currency) {
      this.amount = amount;
      this.currency = currency;
    }
    
    public String toString() {
      return amount+" "+currency;
    }
  }
}
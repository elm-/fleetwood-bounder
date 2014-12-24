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
package com.heisenberg.test.plugin.datatype;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.DataTypes;
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.WorkflowEngineConfiguration;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.impl.plugin.AbstractActivityType;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Label;
import com.heisenberg.impl.script.Script;
import com.heisenberg.impl.script.ScriptResult;
import com.heisenberg.impl.script.ScriptService;
import com.heisenberg.test.WorkflowTest;


/**
 * @author Walter White
 */
public class DataTypeScriptTest extends WorkflowTest {
  
  @Test
  public void testScript() {
    WorkflowEngine workflowEngine = new WorkflowEngineConfiguration()
      .registerJavaBeanType(Money.class)
      .registerActivityType(new ScriptActivity())
      .buildProcessEngine();

    DataTypes dataTypes = workflowEngine.getDataTypes();
    
    WorkflowBuilder w = workflowEngine.newWorkflow();
    
    w.newVariable()
      .id("m")
      .dataType(dataTypes.javaBean(Money.class));
    
    w.newActivity()
      .activityType(new ScriptActivity())
      .id("a");

    String workflowId = w.deploy();
    
    Map<String,Object> fiveDollars = new HashMap<>();
    fiveDollars.put("amount", 5d);
    fiveDollars.put("currency", "USD");
    
    workflowEngine.newStart()
      .workflowId(workflowId)
      .variableValue("m", new Money(5, "USD"))
      .startWorkflowInstance();

    assertEquals("It costs 5, which is in USD\nAnd mmmoney is 5 USD", scriptResultMessage);
  }
  
  static String scriptResultMessage = null;

  @JsonTypeName("testScript")
  @Label("Script")
  public static class ScriptActivity extends AbstractActivityType {
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      ScriptService scriptService = activityInstance.getServiceRegistry().getService(ScriptService.class);
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

    public int amount;
    public String currency;

    public Money(int amount, String currency) {
      this.amount = amount;
      this.currency = currency;
    }
    
    public String toString() {
      return amount+" "+currency;
    }
  }
}

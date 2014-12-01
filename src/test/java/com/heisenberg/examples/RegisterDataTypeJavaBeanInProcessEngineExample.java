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
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;


/**
 * @author Walter White
 */
public class RegisterDataTypeJavaBeanInProcessEngineExample {

  public static final Logger log = LoggerFactory.getLogger(RegisterDataTypeJavaBeanInProcessEngineExample.class);
  
  @Test
  public void testProcessEngineCustomMoneyType() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerJavaBeanType(CustomMoney.class);

    ProcessBuilder process = processEngine.newProcess();
    
    process.newVariable()
      .id("m")
      .dataTypeJavaBean(CustomMoney.class);
    
    process.newActivity()
      .activityType(new UserTask())
      .id("w");

    String processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();

    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinitionId)
      .variableValue("m", new CustomMoney(5d, "USD")));
  
    VariableInstance m = processInstance.getVariableInstances().get(0);
    CustomMoney money = (CustomMoney) m.getValue();
    assertEquals(5d, money.amount, 0.000001d);
    assertEquals("USD", money.currency);
    assertEquals(CustomMoney.class.getName(), m.getDataTypeId());
  }
  
  @Test
  public void testProcessEngineJavaTypeDeclaration() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerJavaBeanType(CustomMoney.class);

    ProcessBuilder process = processEngine.newProcess();
    
    process.newVariable()
      .id("m")
      .dataTypeJavaBean(CustomMoney.class);
    
    process.newActivity()
      .activityType(new UserTask())
      .id("w");

    String processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();

    Map<String,Object> fiveDollarsJson = new HashMap<>();
    fiveDollarsJson.put("amount", 5d);
    fiveDollarsJson.put("currency", "USD");
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinitionId)
      // This time the json variant is used to set the variable value
      .variableValueJson("m", fiveDollarsJson));
  
    VariableInstance mInstance = processInstance.getVariableInstances().get(0);
    assertEquals(CustomMoney.class.getName(), mInstance.getDataTypeId());
    CustomMoney money = (CustomMoney) mInstance.getValue();
    assertEquals(5d, money.amount, 0.000001d);
    assertEquals("USD", money.currency);
  }

  static class CustomMoney {

    public double amount;
    public String currency;
    
    public CustomMoney() {
    }

    public CustomMoney(double amount, String currency) {
      this.amount = amount;
      this.currency = currency;
    }
  }
}

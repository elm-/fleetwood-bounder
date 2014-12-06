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
package com.heisenberg.test.datatype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.builder.TriggerBuilder;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.api.type.JavaBeanType;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.TriggerBuilderImpl;


/**
 * @author Walter White
 */
public class CustomJavaBeanSerializationTest {

  public static final Logger log = LoggerFactory.getLogger(CustomJavaBeanSerializationTest.class);
  
  @Test
  public void testProcessEngineCustomMoneyType() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerJavaBeanType(CustomMoney.class)
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("m")
      .dataTypeJavaBean(CustomMoney.class);
    
    String processDefinitionId = process.deploy()
      .checkNoErrors()
      .getProcessDefinitionId();
    
    CustomMoney startProcessMoney = new CustomMoney(5d, "USD");
  
    // start a process instance supplying a java bean object as the variable value
    TriggerBuilder trigger = processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .variableValue("m", startProcessMoney, new JavaBeanType(CustomMoney.class));

    JsonService jsonService = ((ProcessEngineImpl)processEngine).getJsonService();
    String triggerJson = jsonService.objectToJsonStringPretty(trigger);
    log.debug("Serialized trigger message that can be sent to remote REST API:");
    log.debug(triggerJson);

    TriggerBuilderImpl triggerImpl = jsonService.jsonToObject(triggerJson, TriggerBuilderImpl.class);
    triggerImpl.deserialize((ProcessEngineImpl)processEngine);
    
    ProcessInstance processInstance = trigger
      .startProcessInstance();
  
    VariableInstance m = processInstance.getVariableInstances().get(0);
    CustomMoney variableInstanceMoney = (CustomMoney) m.getValue();
    assertSame(startProcessMoney, variableInstanceMoney);
    assertEquals(5d, variableInstanceMoney.amount, 0.000001d);
    assertEquals("USD", variableInstanceMoney.currency);
    JavaBeanType javaBeanType = (JavaBeanType) m.getDataType();
    assertEquals(CustomMoney.class, javaBeanType.javaClass);
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

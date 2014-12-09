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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.api.type.DataTypeReference;
import com.heisenberg.api.type.JavaBeanType;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.plugin.DataTypeRegistration;


/**
 * @author Walter White
 */
public class DataTypeJavaBeanTest {

  public static final Logger log = LoggerFactory.getLogger(DataTypeJavaBeanTest.class);
  
  @Test
  public void testProcessEngineCustomMoneyType() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerJavaBeanType(Money.class)
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("m")
      .dataTypeJavaBean(Money.class);
    
    String processDefinitionId = process.deploy()
      .checkNoErrors()
      .getProcessDefinitionId();
    
    Money startProcessMoney = new Money(5d, "USD");
  
    // start a process instance supplying a java bean object as the variable value
    ProcessInstance processInstance = processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .variableValue("m", startProcessMoney)
      .startProcessInstance();
  
    VariableInstance m = processInstance.getVariableInstances().get(0);
    Money variableInstanceMoney = (Money) m.getValue();
    assertSame(startProcessMoney, variableInstanceMoney);
    assertEquals(5d, variableInstanceMoney.amount, 0.000001d);
    assertEquals("USD", variableInstanceMoney.currency);
    DataTypeReference dataTypeReference = (DataTypeReference) m.getDataType();
    JavaBeanType javaBeanType = (JavaBeanType) dataTypeReference.delegate;
    assertEquals(Money.class, javaBeanType.javaClass);

    // create the json representation of a custom money object
    Map<String,Object> customMoneyJson = new HashMap<>();
    customMoneyJson.put("amount", 6);
    customMoneyJson.put("currency", "EUR");

    // start a process instance supplying a json representation as the variable value
    processInstance = processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .variableValue("m", customMoneyJson, Money.class)
      .startProcessInstance();
  
    VariableInstance mInstance = processInstance.getVariableInstances().get(0);
    javaBeanType = (JavaBeanType) m.getDataType();
    assertEquals(Money.class, javaBeanType.javaClass);
    Money money = (Money) mInstance.getValue();
    assertEquals(6d, money.amount, 0.000001d);
    assertEquals("EUR", money.currency);
  }

  static class Money {
    public double amount;
    public String currency;
    public Money() {
    }
    public Money(double amount, String currency) {
      this.amount = amount;
      this.currency = currency;
    }
  }
}

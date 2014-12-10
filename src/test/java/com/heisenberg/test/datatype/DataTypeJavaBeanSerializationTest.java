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
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.TriggerBuilderImpl;
import com.heisenberg.impl.type.DataTypeReference;
import com.heisenberg.impl.type.JavaBeanType;


/**
 * @author Walter White
 */
public class DataTypeJavaBeanSerializationTest {

  public static final Logger log = LoggerFactory.getLogger(DataTypeJavaBeanSerializationTest.class);
  
  @Test
  public void testProcessEngineCustomMoneyType() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerJavaBeanType(Money.class)
      .buildProcessEngine();
    
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("m")
      .dataType(process.newDataTypeJavaBean(Money.class));
    
    String processDefinitionId = process.deploy()
      .checkNoErrors()
      .getProcessDefinitionId();
    
    Money startProcessMoney = new Money(5, "USD");

    JsonService jsonService = ((ProcessEngineImpl)processEngine).getJsonService();

    // start a process instance supplying a java bean object as the variable value
    TriggerBuilder trigger = processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .variableValue("m", startProcessMoney, Money.class);

    String triggerJson = jsonService.objectToJsonStringPretty(trigger);
    log.debug("Serialized trigger message that can be sent to remote REST API:");
    log.debug(triggerJson);

    TriggerBuilderImpl triggerImpl = jsonService.jsonToObject(triggerJson, TriggerBuilderImpl.class);
    triggerImpl.deserialize((ProcessEngineImpl)processEngine);
    
    ProcessInstance processInstance = triggerImpl
      .startProcessInstance();
  
    VariableInstance m = processInstance.getVariableInstances().get(0);
    Money variableInstanceMoney = (Money) m.getValue();
    assertEquals(startProcessMoney.amount, variableInstanceMoney.amount, 0.0001);
    assertEquals(startProcessMoney.currency, variableInstanceMoney.currency);
    assertEquals(5d, variableInstanceMoney.amount, 0.000001d);
    assertEquals("USD", variableInstanceMoney.currency);
    DataTypeReference dataTypeReference = (DataTypeReference) m.getDataType();
    JavaBeanType javaBeanType = (JavaBeanType) dataTypeReference.delegate;
    assertEquals(Money.class, javaBeanType.javaClass);
  }

  static class Money {
    public int amount;
    public String currency;
    public Money() {
    }
    public Money(int amount, String currency) {
      this.amount = amount;
      this.currency = currency;
    }
  }
}

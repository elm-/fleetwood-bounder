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
package com.heisenberg.test.db;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.api.plugin.DataTypes;
import com.heisenberg.impl.util.Lists;


/**
 * @author Walter White
 */
public class MongoDataTypeListJavaBeanTest {

  public static final Logger log = LoggerFactory.getLogger(MongoDataTypeListJavaBeanTest.class);
  
  @SuppressWarnings("unchecked")
  @Test
  public void testVariableStoringListOfBeans() {
    ProcessEngine processEngine = new MongoProcessEngineConfiguration()
      .registerJavaBeanType(Money.class)
      .server("localhost", 27017)
      .buildProcessEngine();

    DataTypes types = processEngine.getDataTypes();
    
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("v")
      .dataType(types.list(types.javaBean(Money.class)));
    
    String processDefinitionId = process.deploy()
      .checkNoErrors()
      .getProcessDefinitionId();

    List<Money> moneys = Lists.of(new Money(5, "USD"), new Money(6, "EUR"));
    ProcessInstance processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", moneys, types.list(types.javaBean(Money.class)))
      .startProcessInstance();
  
    VariableInstance v = processInstance.getVariableInstances().get(0);
    List<Money> values = (List<Money>) v.getValue();
    assertEquals(5, values.get(0).amount);
    assertEquals("USD", values.get(0).currency);
    assertEquals(6, values.get(1).amount);
    assertEquals("EUR", values.get(1).currency);
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
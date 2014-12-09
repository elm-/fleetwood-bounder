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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.api.type.JavaBeanType;
import com.heisenberg.api.type.ListType;
import com.heisenberg.impl.util.Lists;


/**
 * @author Walter White
 */
public class DbDataTypeListJavaBeanTest {

  public static final Logger log = LoggerFactory.getLogger(DbDataTypeListJavaBeanTest.class);
  
  @Test
  public void testVariableStoringListOfBeans() {
    ProcessEngine processEngine = new MongoProcessEngineConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("v")
      .dataType(new ListType(new JavaBeanType(Money.class)));
    
    String processDefinitionId = process.deploy()
      .checkNoErrors()
      .getProcessDefinitionId();

    ProcessInstance processInstance = processEngine.newTrigger()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", Lists.of(new Money(5, "USD"), new Money(6, "EUR")))
      .startProcessInstance();
  
    VariableInstance v = processInstance.getVariableInstances().get(0);
    assertEquals(Lists.of(new Money(5, "USD"), new Money(6, "EUR")), v.getValue());
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

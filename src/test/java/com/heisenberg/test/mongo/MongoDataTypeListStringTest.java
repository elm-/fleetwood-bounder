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
package com.heisenberg.test.mongo;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.DataTypes;
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.impl.util.Lists;
import com.heisenberg.mongo.MongoWorkflowEngineConfiguration;


/**
 * @author Walter White
 */
public class MongoDataTypeListStringTest {

  public static final Logger log = LoggerFactory.getLogger(MongoDataTypeListStringTest.class);
  
  @SuppressWarnings("unchecked")
  @Test
  public void testVariableStoringListOfBeans() {
    WorkflowEngine workflowEngine = new MongoWorkflowEngineConfiguration()
      .registerJavaBeanType(Money.class)
      .server("localhost", 27017)
      .buildProcessEngine();

    DataTypes dataTypes = workflowEngine.getDataTypes();
    
    WorkflowBuilder process = workflowEngine.newWorkflow();
    
    process.newVariable()
      .id("v")
      .dataType(dataTypes.list(dataTypes.javaBean(Money.class)));
    
    String processDefinitionId = process.deploy()
      .checkNoErrors()
      .getWorkflowId();

    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
      .variableValue("v", Lists.of(new Money(1,"EUR"), new Money(2,"USD")))
      .startWorkflowInstance();
  
    VariableInstance v = workflowInstance.getVariableInstances().get(0);
    List<Money> moneyList = (List<Money>) v.getValue();
    assertEquals(1, moneyList.get(0).amount);
    assertEquals("EUR", moneyList.get(0).currency);
    assertEquals(2, moneyList.get(1).amount);
    assertEquals("USD", moneyList.get(1).currency);
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

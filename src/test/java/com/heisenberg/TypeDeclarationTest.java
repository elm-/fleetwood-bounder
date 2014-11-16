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
package com.heisenberg;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.definition.VariableDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.api.type.ChoiceDescriptor;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.spi.InvalidApiValueException;


/**
 * @author Walter White
 */
public class TypeDeclarationTest {

  @Test
  public void testProcessEngineJavaTypeDeclaration() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerType(Money.class);

    // prepare the ingredients
    VariableDefinition m = new VariableDefinition()
      .name("m")
      .type(Money.class);
    
    // cook a process batch
    ProcessDefinition processDefinition = new ProcessDefinition()
      .variable(m);

    String processDefinitionId = processEngine
      .deployProcessDefinition(processDefinition)
      .checkNoErrorsAndNoWarnings()
      .processDefinitionId;
    
    Map<String,Object> fiveDollars = new HashMap<>();
    fiveDollars.put("amount", 5d);
    fiveDollars.put("currency", "USD");
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("m", new Money(5d, "USD")));
    
    VariableInstance mInstance = processInstance.variableInstances.get(0);
    assertEquals(Money.class.getName(), mInstance.typeRefId);
    Money money = (Money) mInstance.value;
    assertEquals(5d, money.amount, 0.000001d);
    assertEquals("USD", money.currency);
  }
  
  @Test
  public void testProcessChoiceDeclaration() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(new Go())
      .registerActivityType(new Wait());

    // prepare the ingredients
    ChoiceDescriptor countryType = new ChoiceDescriptor()
      .id("country")
      .option("be", "Belgium")
      .option("us", "US")
      .option("de", "Germany")
      .option("fr", "France");
    
    VariableDefinition c = new VariableDefinition()
      .type("country")
      .name("c");

    // cook the process
    ProcessDefinition processDefinition = new ProcessDefinition()
      .type(countryType)
      .variable(c);

    String processDefinitionId = processEngine
      .deployProcessDefinition(processDefinition)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();

    // a valid value
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("c", "be"));
    VariableInstance cInstance = processInstance.variableInstances.get(0);
    assertEquals("country", cInstance.typeRefId);
    assertEquals("be", cInstance.value);

    // a valid value
    try {
      processEngine.startProcessInstance(new StartProcessInstanceRequest()
        .processDefinitionRefId(processDefinitionId)
        .variableValue("c", "xxx"));
      Assert.fail("expectedException");
    } catch (Exception e) {
      assertEquals(InvalidApiValueException.class, e.getCause().getClass());
    }
  }
}

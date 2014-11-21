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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.definition.ProcessBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.DataType;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.type.ChoiceType;


/**
 * @author Walter White
 */
public class SpiDataTypePluggabilityTest {

  public static final Logger log = LoggerFactory.getLogger(SpiDataTypePluggabilityTest.class);
  
  @Test
  public void testProcessEngineJavaTypeDeclaration() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerJavaBeanType(Money.class);

    ProcessBuilder processBuilder = processEngine.newProcess();
    
    processBuilder.newVariable()
      .name("m")
      .dataTypeJavaBean(Money.class);
    
    String processDefinitionId = processEngine
      .deployProcessDefinition(processBuilder)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    Map<String,Object> fiveDollars = new HashMap<>();
    fiveDollars.put("amount", 5d);
    fiveDollars.put("currency", "USD");
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("m", new Money(5d, "USD")));
    
    VariableInstance mInstance = processInstance.getVariableInstances().get(0);
    assertEquals(Money.class.getName(), mInstance.getDataTypeId());
    Money money = (Money) mInstance.getValue();
    assertEquals(5d, money.amount, 0.000001d);
    assertEquals("USD", money.currency);
  }
  
  @Test
  public void testProcessChoiceDeclaration() {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerType(new ChoiceType()
        .id("country")
        .option("be", "Belgium")
        .option("us", "US")
        .option("de", "Germany")
        .option("fr", "France"));

    Json json = processEngine.json;
    DataType countryType = processEngine.dataTypes.get("country");
    log.debug("From oss on-premise to SaaS process builder:");
    log.debug(json.objectToJsonStringPretty(countryType)+"\n");

    ProcessBuilder process = processEngine.newProcess();
    
    process.newVariable()
      .type("country")
      .name("c");

    String processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();

    // a valid value
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("c", "be"));
    VariableInstance cInstance = processInstance.getVariableInstances().get(0);
    assertEquals("country", cInstance.getDataTypeId());
    assertEquals("be", cInstance.getValue());

    // an invalid value
    try {
      processEngine.startProcessInstance(new StartProcessInstanceRequest()
        .processDefinitionRefId(processDefinitionId)
        .variableValue("c", "xxx"));
      Assert.fail("expected exception");
    } catch (Exception e) {
      assertEquals(InvalidApiValueException.class, e.getCause().getClass());
    }
  }
}

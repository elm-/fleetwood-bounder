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

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.api.type.ChoiceType;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.InvalidValueException;
import com.heisenberg.api.util.ProcessDefinitionId;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.json.Json;
import com.heisenberg.test.Wait;


/** Shows how to configure a type and register it with an id.
 *  
 * @author Walter White
 */
public class RegisterDataTypeConfigurationInProcessEngineExample {

  public static final Logger log = LoggerFactory.getLogger(RegisterDataTypeConfigurationInProcessEngineExample.class);
  
  @Test
  public void testProcessDefinitionTypeConfiguration() {
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

    process.newActivity()
      .activityType(Wait.INSTANCE)
      .name("w");

    ProcessDefinitionId pdid = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();

    // a valid value
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(pdid)
      .variableValue("c", "be"));
    VariableInstance cInstance = processInstance.getVariableInstances().get(0);
    assertEquals("country", cInstance.getDataTypeId());
    assertEquals("be", cInstance.getValue());

    // an invalid value
    try {
      processEngine.startProcessInstance(new StartProcessInstanceRequest()
        .processDefinitionId(pdid)
        .variableValue("c", "xxx"));
      Assert.fail("expected exception");
    } catch (Exception e) {
      assertEquals(InvalidValueException.class, e.getCause().getClass());
    }
  }
}

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

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.definition.ProcessBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.type.ChoiceType;

/**
 * @author Walter White
 */
public class ChoiceTypeTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(new Go())
      .registerActivityType(new Wait());

    ChoiceType countryType = new ChoiceType()
      .id("country")
      .option("be", "Belgium")
      .option("us", "US")
      .option("de", "Germany")
      .option("fr", "France");

    ProcessBuilder processBuilder = processEngine.newProcess()
            .type(countryType);
    
    processBuilder.newVariable()
      .type("country")
      .name("c");

    String processDefinitionId = processEngine
      .deployProcessDefinition(processBuilder)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("c", "be"));
  }

}

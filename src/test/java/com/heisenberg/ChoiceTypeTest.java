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
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.definition.VariableDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.type.ChoiceDescriptor;
import com.heisenberg.engine.memory.MemoryProcessEngine;

/**
 * @author Walter White
 */
public class ChoiceTypeTest {
  
  @Test
  public void testOne() {
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
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionRefId(processDefinitionId)
      .variableValue("c", "be"));
  }
}

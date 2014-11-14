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

import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.VariableDefinitionImpl;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.spi.Type;
import com.heisenberg.type.Reference;

/**
 * @author Walter White
 */
public class ReferenceTypeTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new MemoryProcessEngine();
    
    processEngine.getServices();
    
    // prepare the ingredients
    VariableDefinitionImpl t = new VariableDefinitionImpl()
      .type(Type.TEXT);
    
    // cook the process
    ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl()
      .variable(t);

    processDefinition = processEngine.saveProcessDefinition(processDefinition);
    
    ProcessInstanceImpl processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinition.getId())
      .variableValue(t.getId(), new Reference().reference("reference-to-object")));

    
  }
}

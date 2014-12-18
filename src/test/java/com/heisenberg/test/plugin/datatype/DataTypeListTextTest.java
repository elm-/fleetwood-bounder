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
package com.heisenberg.test.plugin.datatype;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.impl.type.ListType;
import com.heisenberg.impl.type.TextType;
import com.heisenberg.impl.util.Lists;
import com.heisenberg.memory.MemoryProcessEngine;


/**
 * @author Walter White
 */
public class DataTypeListTextTest {

  public static final Logger log = LoggerFactory.getLogger(DataTypeListTextTest.class);
  
  @Test
  public void testVariableStoringListOfTexts() {
    ProcessEngine processEngine = new MemoryProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("v")
      .dataType(new ListType(TextType.INSTANCE));
    
    String processDefinitionId = process.deploy()
      .checkNoErrors()
      .getProcessDefinitionId();

    ProcessInstance processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", Lists.of("Hello", "World"))
      .startProcessInstance();
  
    VariableInstance v = processInstance.getVariableInstances().get(0);
    assertEquals(Lists.of("Hello", "World"), v.getValue());
  }
}

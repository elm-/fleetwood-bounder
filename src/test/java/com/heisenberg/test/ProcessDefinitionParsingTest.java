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
package com.heisenberg.test;

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;


/**
 * @author Walter White
 */
public class ProcessDefinitionParsingTest {

  @Test
  public void testActivityDefinitionWithoutName() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class);
    
    // cook the process
    ProcessDefinitionBuilder processBuilder = processEngine.newProcessDefinition();
    
    processBuilder.newActivity()
      .activityType(new Go());

    TestHelper.assertTextPresent("Activity has no id", processEngine
        .deployProcessDefinition(processBuilder)
        .getIssueReport());
  }

  @Test
  public void testActivityDefinitionWithoutType() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class);
    
    ProcessDefinitionBuilder processBuilder = processEngine.newProcessDefinition();
    
    processBuilder.newActivity()
      .id("a");

    TestHelper.assertTextPresent("Activity 'a' has no activityType configured", processEngine
        .deployProcessDefinition(processBuilder)
        .getIssueReport());
  }
}

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
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.engine.memory.MemoryProcessEngine;


/**
 * @author Walter White
 */
public class ProcessDefinitionParsingTest {

  @Test
  public void testActivityDefinitionWithoutName() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(new Go());
    
    // cook the process
    ProcessDefinition processDefinition = new ProcessDefinition()
      .activity(new ActivityDefinition()
      .type(Go.TYPE_ID));

    TestHelper.assertTextPresent("Activity has no name", processEngine
        .deployProcessDefinition(processDefinition)
        .getIssueReport());
  }

  @Test
  public void testActivityDefinitionWithoutType() {
    ProcessEngine processEngine = new MemoryProcessEngine()
      .registerActivityType(new Go());
    
    // cook the process
    ProcessDefinition processDefinition = new ProcessDefinition()
      .activity(new ActivityDefinition()
      .name("a"));

    TestHelper.assertTextPresent("Activity /a has invalid type", processEngine
        .deployProcessDefinition(processDefinition)
        .getIssueReport());
  }
}

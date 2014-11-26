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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.test.TestHelper;


/**
 * @author Walter White
 */
public class ProcessDefinitionValidationExample {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessDefinitionValidationExample.class);

  @Test
  public void testActivityDefinitionWithoutName() {
    ProcessEngine processEngine = new MemoryProcessEngine();
    
    // cook the process
    ProcessBuilder processBuilder = processEngine.newProcess();
    
    processBuilder.newActivity()
      .activityType(StartEvent.INSTANCE);

    processBuilder.newActivity()
      .id("a");
  
    processBuilder.newActivity()
      .id("b")
      .activityType(EndEvent.INSTANCE);
    
    processBuilder.newTransition()
      .from("a")
      .to("non existing");

    String issueReport = processEngine
        .deployProcessDefinition(processBuilder)
        .getIssueReport();
    
    log.debug(issueReport);
    
    TestHelper.assertTextPresent("Activity has no id", issueReport);
  }
}

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
package com.heisenberg.test.validation;

import static com.heisenberg.test.TestHelper.assertTextPresent;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
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
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newActivity()
      .activityType(StartEvent.INSTANCE);

    process.newActivity()
      .id("a");
  
    process.newActivity()
      .id("b")
      .activityType(EndEvent.INSTANCE);
    
    process.newTransition()
      .from("a")
      .to("non existing");

    String issueReport = process.deploy()
        .getIssueReport();
    
    log.debug(issueReport);
    
    assertTextPresent("Activity has no id", issueReport);
    assertTextPresent("Activity 'a' has no activityType configured", issueReport);
    assertTextPresent("Transition has an invalid value for 'to' (non existing) : Should be one of [a, b]", issueReport);
  }
  
//  @Test
//  public void testActivityDefinitionWithoutName() {
//    ProcessEngine processEngine = new MemoryProcessEngine()
//      .registerActivityType(Go.class);
//    
//    // cook the process
//    ProcessDefinitionBuilder processBuilder = processEngine.newProcessDefinition();
//    
//    processBuilder.newActivity()
//      .activityType(new Go());
//
//    TestHelper.assertTextPresent("Activity has no id", processEngine
//        .deployProcessDefinition(processBuilder)
//        .getIssueReport());
//  }
//
//  @Test
//  public void testActivityDefinitionWithoutType() {
//    ProcessEngine processEngine = new MemoryProcessEngine()
//      .registerActivityType(Go.class);
//    
//    ProcessDefinitionBuilder processBuilder = processEngine.newProcessDefinition();
//    
//    processBuilder.newActivity()
//      .id("a");
//
//    TestHelper.assertTextPresent("Activity 'a' has no activityType configured", processEngine
//        .deployProcessDefinition(processBuilder)
//        .getIssueReport());
//  }

}

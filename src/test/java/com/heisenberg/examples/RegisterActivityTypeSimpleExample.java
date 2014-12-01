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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;


/**
 * @author Walter White
 */
public class RegisterActivityTypeSimpleExample {
  
  public static final Logger log = LoggerFactory.getLogger(RegisterActivityTypeSimpleExample.class);

  @Test
  public void testSpiActivityPluggability() throws Exception {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerActivityType(MyCustomType.class);
    
    ProcessBuilder process = processEngine.newProcess();
    
    process.newActivity()
      .id("a")
      .activityType(new MyCustomType());
    
    String processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .getProcessDefinitionId();
    
    processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinitionId));
    
    assertEquals("Leroy was here", message);
  }
  
  static String message; 

  public static class MyCustomType extends AbstractActivityType {
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      message = "Leroy was here";
    }

    @Override
    public String getTypeId() {
      return "myCustomType";
    }
  }
}

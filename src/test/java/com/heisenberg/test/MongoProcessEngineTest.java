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
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.type.TextType;
import com.heisenberg.api.util.ProcessDefinitionId;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration;


/**
 * @author Walter White
 */
public class MongoProcessEngineTest {
  
  @Test
  public void testMongoProcessEngine() {
    ProcessEngine processEngine = new MongoConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine()
      .registerActivityType(Go.class)
      .registerActivityType(Wait.class);
    
    ProcessBuilder process = processEngine.newProcess();
  
    process.newVariable()
      .name("t")
      .dataType(TextType.INSTANCE);
  
    Go go = new Go()
      .placeBinding(new Binding<String>().expression("t.toLowerCase()"));
    
    process.newActivity()
      .activityType(go)
      .name("go");
    
    process.newActivity()
      .activityType(Wait.INSTANCE)
      .name("wait1");
    
    process.newActivity()
      .activityType(Wait.INSTANCE)
      .name("wait2");
    
    process.newTransition()
      .from("wait1")
      .to("wait2");
    
    ProcessDefinitionId processDefinitionId = processEngine
      .deployProcessDefinition(process)
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();

  }

}

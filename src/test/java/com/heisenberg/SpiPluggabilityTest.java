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

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.definition.ProcessBuilder;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.AbstractActivityType;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.Binding;
import com.heisenberg.spi.Label;
import com.heisenberg.spi.SpiDescriptor;
import com.heisenberg.spi.Type;
import com.heisenberg.type.TextType;


/**
 * @author Walter White
 */
public class SpiPluggabilityTest {
  
  public static final Logger log = LoggerFactory.getLogger(SpiPluggabilityTest.class);

  @Test
  public void testOne() {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerActivityType(MyCustomType.class);
    
    Json json = new Json(processEngine);
    SpiDescriptor spiDescriptor = processEngine.activityDescriptors.get(MyCustomType.class.getName());
    log.debug("From oss on-premise to SaaS process builder:");
    log.debug(json.objectToJsonStringPretty(spiDescriptor)+"\n");
    
    log.debug("SaaS process builder shows the activity in the pallete");

    ProcessBuilder processBuilder = processEngine.newProcess();
    processBuilder.newActivity()
      .name("a")
      .activityType(new MyCustomType()
        .functionName("functOne")
        .parameterOne(new Binding<TextType>().variableName("v"))
      );
    processBuilder.newVariable()
      .name("v")
      .type(Type.TEXT);

    log.debug("The process as it is deployed into the engine:");
    String processJson = json.objectToJsonStringPretty(processBuilder);
    log.debug(processJson+"\n");
    
    ProcessDefinitionImpl processDefinition = json.jsonToObject(processJson, ProcessDefinitionImpl.class);
    assertNotNull(processDefinition);
    
    MyCustomType myCustomActivity = (MyCustomType) processDefinition.activityDefinitions.get(0).activityType;
    assertEquals("functOne", myCustomActivity.functionName);
    assertEquals("v", myCustomActivity.parameterOne.variableName);
  }

  public static class MyCustomType extends AbstractActivityType {

    @Label("Function name")
    String functionName;

    @Label("Parameter one")
    Binding<TextType> parameterOne; 
    
    @Override
    public void start(ActivityInstanceImpl activityInstance) {
      log.debug("Function name: "+functionName);
      log.debug("Parameter one: "+parameterOne.getValue(activityInstance, String.class));
    }
    
    public MyCustomType functionName(String functionName) {
      this.functionName = functionName;
      return this;
    }
    
    public MyCustomType parameterOne(Binding<TextType> parameterOne) {
      this.parameterOne = parameterOne;
      return this;
    }
  }
}

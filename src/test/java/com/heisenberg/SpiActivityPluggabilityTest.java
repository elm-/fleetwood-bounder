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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.definition.ProcessBuilder;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.AbstractActivityType;
import com.heisenberg.spi.Binding;
import com.heisenberg.spi.ConfigurationField;
import com.heisenberg.spi.SpiDescriptor;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class SpiActivityPluggabilityTest {
  
  public static final Logger log = LoggerFactory.getLogger(SpiActivityPluggabilityTest.class);

  @Test
  public void testOne() throws Exception {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerJavaBeanType(Money.class)
      .registerActivityType(MyCustomType.class);
    
    Json json = new Json(processEngine);
    SpiDescriptor spiDescriptor = processEngine.activityDescriptors.get(MyCustomType.class.getName());
    log.debug("From oss on-premise to SaaS process builder:");
    log.debug(json.objectToJsonStringPretty(spiDescriptor)+"\n");
    
    log.debug("SaaS process builder shows the activity in the pallete");

    ProcessBuilder processBuilder = processEngine.newProcess();
    ActivityDefinitionImpl a = (ActivityDefinitionImpl) processBuilder.newActivity()
      .name("a")
      .activityType(new MyCustomType()
        .functionName("functOne")
        .parameterOne(new Binding<String>().variableName("v"))
      );
    processBuilder.newVariable()
      .name("v")
      .type(Type.TEXT);

    log.debug("The process as it is deployed into the engine:");
    String processJson = json.objectToJsonStringPretty(processBuilder);
    log.debug(processJson+"\n");

    ProcessDefinitionImpl processDefinition = json.jsonToObject(processJson, ProcessDefinitionImpl.class);
    log.debug(processJson+"\n");

    String aJsonText = json.objectToJsonStringPretty(a.activityType);
    log.debug(aJsonText+"\n");

    @SuppressWarnings("unchecked")
    Map<String,Object> aJsonMap = (Map<String,Object>)json.objectMapper.readValue(aJsonText, Map.class);
    
    processBuilder = processEngine.newProcess();
    processBuilder.newActivity()
      .activityTypeJson(aJsonMap);
    
    processDefinition = (ProcessDefinitionImpl) processBuilder;
    
    MyCustomType myCustomActivity = (MyCustomType) processDefinition.activityDefinitions.get(0).activityType;
    assertEquals("functOne", myCustomActivity.functionName);
    assertEquals("v", myCustomActivity.parameterOne.variableName);
  }

  public static class MyCustomType extends AbstractActivityType {
    
    @ConfigurationField("Function name")
    String functionName;

    @ConfigurationField("Parameter one")
    Binding<String> parameterOne;
    
    @ConfigurationField("Billable bills")
    List<Binding<Money>> moneyBindings;
    
    @Override
    public void start(ActivityInstanceImpl activityInstance) {
      log.debug("Function name: "+functionName);
      log.debug("Parameter one: "+parameterOne.getValue(activityInstance));
    }
    
    public MyCustomType functionName(String functionName) {
      this.functionName = functionName;
      return this;
    }
    
    public MyCustomType parameterOne(Binding<String> parameterOne) {
      this.parameterOne = parameterOne;
      return this;
    }
  }
}

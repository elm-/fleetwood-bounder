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

import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.type.TextType;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionValidator;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.jsondeprecated.JsonServiceImpl;
import com.heisenberg.impl.plugin.TypeDescriptor;


/**
 * @author Walter White
 */
public class RegisterActivityTypeConfigurationExample {
  
  public static final Logger log = LoggerFactory.getLogger(RegisterActivityTypeConfigurationExample.class);

  @Test
  public void testSpiActivityPluggability() throws Exception {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerActivityType(MyCustomType.class);
    
    JsonServiceImpl jacksonJsonService = processEngine.jsonService;
    TypeDescriptor spiDescriptor = processEngine.activityTypeDescriptorsByTypeId.get("myCustomType");
    log.debug("From oss on-premise to SaaS process builder:");
    log.debug(jacksonJsonService.objectToJsonStringPretty(spiDescriptor)+"\n");
    
    log.debug("SaaS process builder shows the activity in the pallete");

    ProcessDefinitionBuilder processBuilder = processEngine.newProcessDefinition();
    ActivityDefinitionImpl a = (ActivityDefinitionImpl) processBuilder.newActivity()
      .id("a")
      .activityType(new MyCustomType()
        .functionName("functOne")
        .parameterOne(new Binding<String>().variableDefinitionId("v"))
      );
    processBuilder.newVariable()
      .id("v")
      .dataType(TextType.INSTANCE);

    log.debug("The process as it is deployed into the engine:");
    String processJson = jacksonJsonService.objectToJsonStringPretty(processBuilder);
    log.debug(processJson+"\n");

    ProcessDefinitionImpl processDefinition = jacksonJsonService.jsonToObject(processJson, ProcessDefinitionImpl.class);
    log.debug(processJson+"\n");

    String aJsonText = jacksonJsonService.objectToJsonStringPretty(a.activityType);
    log.debug(aJsonText+"\n");

    @SuppressWarnings("unchecked")
    Map<String,Object> aJsonMap = (Map<String,Object>)jacksonJsonService.objectMapper.readValue(aJsonText, Map.class);
    
    processBuilder = processEngine.newProcessDefinition();
    processBuilder.newActivity()
      .activityTypeJson(aJsonMap);
    
    processDefinition = (ProcessDefinitionImpl) processBuilder;
    processDefinition.visit(new ProcessDefinitionValidator(processEngine));
    
    MyCustomType myCustomActivity = (MyCustomType) processDefinition.activityDefinitions.get(0).activityType;
    assertEquals("functOne", myCustomActivity.functionName);
    assertEquals("v", myCustomActivity.parameterOne.variableDefinitionId);
  }

  @JsonTypeName("myCustomType")
  public static class MyCustomType extends AbstractActivityType {
    
    @ConfigurationField("Function name")
    String functionName;

    @ConfigurationField("Parameter one")
    Binding<String> parameterOne;

    @Override
    public void start(ControllableActivityInstance activityInstance) {
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

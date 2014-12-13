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
package com.heisenberg.test.plugin.activitytype;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.activities.Description;
import com.heisenberg.api.activities.Label;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionValidator;
import com.heisenberg.impl.type.TextType;


/**
 * @author Walter White
 */
public class ActivityTypeBindingConfigurationTest {
  
  public static final Logger log = LoggerFactory.getLogger(ActivityTypeBindingConfigurationTest.class);
  
  static List<String> executedConfigurations = new ArrayList<>();
  
  @JsonTypeName("myBindingActivityTypeId")
  @Label("My dynamically configurable activity")
  @Description("Dynamic configuration means that the value used at runtime is derived from process variable data")
  public static class MyBindingActivityType extends AbstractActivityType {
    
    @ConfigurationField
    @Label("Place")
    Binding<String> place;
    
    public MyBindingActivityType() {
    }
    public MyBindingActivityType(Binding<String> place) {
      this.place = place;
    }
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      executedConfigurations.add(activityInstance.getValue(place));
    }
  }
  
  @Test 
  public void testConfigurableActivityTypeExecution() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerConfigurableActivityType(new MyBindingActivityType())
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("v")
      .dataType(new TextType());
    
    Binding<String> expressionBinding = new Binding<String>()
            .expression("v.toLowerCase()");
    
    process.newActivity()
      .activityType(new MyBindingActivityType(expressionBinding))
      .id("a");
  
    String processDefinitionId = process.deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    executedConfigurations.clear();

    processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", "Hello World")
      .startProcessInstance();
    
    List<String> expectedConfigurations = new ArrayList<>();
    expectedConfigurations.add("hello world");
    
    assertEquals(expectedConfigurations, executedConfigurations);
  }

  @Test 
  public void testConfigurableActivityTypeSerialization() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
    .registerConfigurableActivityType(new MyBindingActivityType())
    .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newActivity()
      .activityType(new MyBindingActivityType(new Binding<String>()
              .expression("v.toLowerCase()")))
      .id("a");

    process.newActivity()
      .activityType(new MyBindingActivityType(new Binding<String>()
              .variableDefinitionId("v")))
      .id("b");

    // serialize the process to json text string and deserialize back to java object
    JsonService jsonService = ((ProcessEngineImpl)processEngine).getJsonService();
    String processDefinitionJsonText = jsonService.objectToJsonStringPretty(process);
    log.debug(processDefinitionJsonText);
    ProcessDefinitionImpl processDefinition = jsonService.jsonToObject(processDefinitionJsonText, ProcessDefinitionImpl.class);
    processDefinition.visit(new ProcessDefinitionValidator((ProcessEngineImpl) processEngine));
    
    ActivityDefinitionImpl a = processDefinition.getActivityDefinition("a");
    MyBindingActivityType myConfigurableA = (MyBindingActivityType) a.activityType;
    assertEquals("v.toLowerCase()", myConfigurableA.place.expression);

    ActivityDefinitionImpl b = processDefinition.getActivityDefinition("b");
    MyBindingActivityType myConfigurableB = (MyBindingActivityType) b.activityType;
    assertEquals("v", myConfigurableB.place.variableDefinitionId);
  }
}

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
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.WorkflowEngineConfiguration;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.definition.ActivityImpl;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.definition.WorkflowValidator;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.plugin.AbstractActivityType;
import com.heisenberg.impl.plugin.Binding;
import com.heisenberg.impl.plugin.ConfigurationField;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Description;
import com.heisenberg.impl.plugin.Label;
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
    WorkflowEngine workflowEngine = new WorkflowEngineConfiguration()
      .registerActivityType(new MyBindingActivityType())
      .buildProcessEngine();

    WorkflowBuilder process = workflowEngine.newWorkflow();
    
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
      .getWorkflowId();
    
    executedConfigurations.clear();

    workflowEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("v", "Hello World")
      .startProcessInstance();
    
    List<String> expectedConfigurations = new ArrayList<>();
    expectedConfigurations.add("hello world");
    
    assertEquals(expectedConfigurations, executedConfigurations);
  }

  @Test 
  public void testConfigurableActivityTypeSerialization() {
    WorkflowEngine workflowEngine = new WorkflowEngineConfiguration()
    .registerActivityType(new MyBindingActivityType())
    .buildProcessEngine();

    WorkflowBuilder process = workflowEngine.newWorkflow();
    
    process.newActivity()
      .activityType(new MyBindingActivityType(new Binding<String>()
              .expression("v.toLowerCase()")))
      .id("a");

    process.newActivity()
      .activityType(new MyBindingActivityType(new Binding<String>()
              .variableDefinitionId("v")))
      .id("b");

    // serialize the process to json text string and deserialize back to java object
    JsonService jsonService = ((WorkflowEngineImpl)workflowEngine).getJsonService();
    String processDefinitionJsonText = jsonService.objectToJsonStringPretty(process);
    log.debug(processDefinitionJsonText);
    WorkflowImpl processDefinition = jsonService.jsonToObject(processDefinitionJsonText, WorkflowImpl.class);
    processDefinition.visit(new WorkflowValidator((WorkflowEngineImpl) workflowEngine));
    
    ActivityImpl a = processDefinition.getActivity("a");
    MyBindingActivityType myConfigurableA = (MyBindingActivityType) a.activityType;
    assertEquals("v.toLowerCase()", myConfigurableA.place.expression);

    ActivityImpl b = processDefinition.getActivity("b");
    MyBindingActivityType myConfigurableB = (MyBindingActivityType) b.activityType;
    assertEquals("v", myConfigurableB.place.variableDefinitionId);
  }
}

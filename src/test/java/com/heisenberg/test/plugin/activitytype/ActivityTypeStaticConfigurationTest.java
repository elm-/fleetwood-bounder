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
import com.heisenberg.impl.plugin.ConfigurationField;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Description;
import com.heisenberg.impl.plugin.Label;


/**
 * @author Walter White
 */
public class ActivityTypeStaticConfigurationTest {
  
  public static final Logger log = LoggerFactory.getLogger(ActivityTypeStaticConfigurationTest.class);
  
  static List<String> executedConfigurations = new ArrayList<>();
  
  @JsonTypeName("myConfigurableActivityTypeId")
  @Label("My configurable activity")
  @Description("Records the configuration in a static member field")
  public static class MyConfigurableActivityType extends AbstractActivityType {
    
    @ConfigurationField(required=true)
    @Label("Configuration property label")
    String configuration;
    
    public MyConfigurableActivityType() {
    }
    public MyConfigurableActivityType(String configuration) {
      this.configuration = configuration;
    }
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      executedConfigurations.add(configuration);
    }
  }
  
  @Test 
  public void testConfigurableActivityTypeExecution() {
    WorkflowEngine workflowEngine = new WorkflowEngineConfiguration()
      .registerActivityType(new MyConfigurableActivityType())
      .buildProcessEngine();

    WorkflowBuilder w = workflowEngine.newWorkflow();
    
    w.newActivity()
      .activityType(new MyConfigurableActivityType("one"))
      .id("a");
  
    w.newActivity()
      .activityType(new MyConfigurableActivityType("two"))
      .id("b");
    
    String workflowId = w.deploy();
    
    executedConfigurations.clear();

    workflowEngine.newStart()
      .workflowId(workflowId)
      .startWorkflowInstance();
    
    List<String> expectedConfigurations = new ArrayList<>();
    expectedConfigurations.add("one");
    expectedConfigurations.add("two");
    
    assertEquals(expectedConfigurations, executedConfigurations);
  }

  @Test 
  public void testConfigurableActivityTypeSerialization() {
    WorkflowEngine workflowEngine = new WorkflowEngineConfiguration()
    .registerActivityType(new MyConfigurableActivityType())
    .buildProcessEngine();

    WorkflowBuilder process = workflowEngine.newWorkflow();
    
    process.newActivity()
      .activityType(new MyConfigurableActivityType("one"))
      .id("a");

    process.newActivity()
      .activityType(new MyConfigurableActivityType("two"))
      .id("b");

    // serialize the process to json text string and deserialize back to java object
    JsonService jsonService = ((WorkflowEngineImpl)workflowEngine).getJsonService();
    String processDefinitionJsonText = jsonService.objectToJsonStringPretty(process);
    log.debug(processDefinitionJsonText);
    WorkflowImpl processDefinition = jsonService.jsonToObject(processDefinitionJsonText, WorkflowImpl.class);
    processDefinition.visit(new WorkflowValidator((WorkflowEngineImpl) workflowEngine));
    
    ActivityImpl a = processDefinition.getActivity("a");
    MyConfigurableActivityType myConfigurableA = (MyConfigurableActivityType) a.activityType;
    assertEquals("one", myConfigurableA.configuration);

    ActivityImpl b = processDefinition.getActivity("b");
    MyConfigurableActivityType myConfigurableB = (MyConfigurableActivityType) b.activityType;
    assertEquals("two", myConfigurableB.configuration);
  }
}

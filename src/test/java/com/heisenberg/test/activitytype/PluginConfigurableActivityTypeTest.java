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
package com.heisenberg.test.activitytype;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.plugin.ActivityTypeDescriptor;


/**
 * @author Walter White
 */
public class PluginConfigurableActivityTypeTest {
  
  public static final Logger log = LoggerFactory.getLogger(PluginConfigurableActivityTypeTest.class);
  
  static List<String> executedConfigurations = new ArrayList<>();
  
  public static class MyConfigurableActivityType extends AbstractActivityType {
    @ConfigurationField("Configuration property label")
    String configuration;
    public MyConfigurableActivityType() {
    }
    public MyConfigurableActivityType(String configuration) {
      this.configuration = configuration;
    }
    @Override
    public String getType() {
      return "myConfigurableActivityTypeId";
    }
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      executedConfigurations.add(configuration);
    }
  }
  
  @Test 
  public void testConfigurableActivityTypeExecution() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerActivityType(MyConfigurableActivityType.class)
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newActivity()
      .activityType(new MyConfigurableActivityType("one"))
      .id("a");
  
    process.newActivity()
      .activityType(new MyConfigurableActivityType("two"))
      .id("b");
    
    String processDefinitionId = process.deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    executedConfigurations.clear();

    processEngine.newProcessInstance()
      .processDefinitionId(processDefinitionId)
      .start();
    
    List<String> expectedConfigurations = new ArrayList<>();
    expectedConfigurations.add("one");
    expectedConfigurations.add("two");
    
    assertEquals(expectedConfigurations, executedConfigurations);
  }

  @Test 
  public void testConfigurableActivityTypeSerialization() {
    ProcessEngine processEngine = new MemoryProcessEngineConfiguration()
      .registerActivityType(MyConfigurableActivityType.class)
      .buildProcessEngine();

    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newActivity()
      .activityType(new MyConfigurableActivityType("one"))
      .id("a");

    process.newActivity()
      .activityType(new MyConfigurableActivityType("two"))
      .id("b");

    // serialize the process to json text string and deserialize back to java object
    JsonService jsonService = processEngine.getJsonService();
    String processDefinitionJsonText = jsonService.objectToJsonStringPretty(process);
    log.debug(processDefinitionJsonText);
    ProcessDefinitionImpl processDefinition = jsonService.jsonToObject(processDefinitionJsonText, ProcessDefinitionImpl.class);
    
    ActivityDefinitionImpl a = processDefinition.getActivityDefinition("a");
    MyConfigurableActivityType myConfigurableA = (MyConfigurableActivityType) a.activityType;
    assertEquals("one", myConfigurableA.configuration);

    ActivityDefinitionImpl b = processDefinition.getActivityDefinition("b");
    MyConfigurableActivityType myConfigurableB = (MyConfigurableActivityType) b.activityType;
    assertEquals("two", myConfigurableB.configuration);
  }
  
  @Test 
  public void testConfigurableActivityTypeDescriptor() {
    ProcessEngineImpl processEngine = (ProcessEngineImpl) new MemoryProcessEngineConfiguration()
      .registerActivityType(MyConfigurableActivityType.class)
      .buildProcessEngine();

    JsonService jsonService = processEngine.getJsonService();
    ActivityTypeDescriptor activityTypeDescriptor = processEngine.activityTypes.getDescriptorsByType().get("myConfigurableActivityTypeId");
    log.debug("From oss on-premise to SaaS process builder:");
    log.debug(jsonService.objectToJsonStringPretty(activityTypeDescriptor)+"\n");
  }

}

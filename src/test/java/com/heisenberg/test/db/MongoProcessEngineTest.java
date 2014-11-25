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
package com.heisenberg.test.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.type.TextType;
import com.heisenberg.api.util.ProcessDefinitionId;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration;
import com.heisenberg.test.TestHelper;
import com.heisenberg.test.Wait;


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
      .id("t")
      .dataType(TextType.INSTANCE);
  
    Go go = new Go()
      .placeBinding(new Binding<String>().expression("t.toLowerCase()"));
    
    process.newActivity()
      .activityType(go)
      .id("go");
    
    process.newActivity()
      .activityType(Wait.INSTANCE)
      .id("wait1");
    
    process.newActivity()
      .activityType(Wait.INSTANCE)
      .id("wait2");
    
    process.newTransition()
      .from("wait1")
      .to("wait2");
    
    ProcessDefinitionId processDefinitionId = processEngine
        .deployProcessDefinition(process)
        .checkNoErrorsAndNoWarnings()
        .getProcessDefinitionId();
      
    List<String> places = new ArrayList<>();
    
    ProcessInstance processInstance = processEngine.startProcessInstance(new StartProcessInstanceRequest()
      .processDefinitionId(processDefinitionId)
      .transientContext("places", places)
      .variableValue("t", "San Fransisco"));
  
    assertNotNull(processInstance.getId());
    assertEquals("Expected 2 but was "+processInstance.getActivityInstances(), 2, processInstance.getActivityInstances().size());
  
    TestHelper.assertActivityInstancesOpen(processInstance, "wait1");
    
    List<String> expectedPlaces = new ArrayList<>();
    expectedPlaces.add("san fransisco");
    assertEquals(expectedPlaces, places);
  }
  
  public static class Go extends AbstractActivityType {
    @ConfigurationField("Place")
    Binding<String> placeBinding;

    public Go placeBinding(Binding<String> placeBinding) {
      this.placeBinding = placeBinding;
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      String place = (placeBinding!=null ? placeBinding.getValue(activityInstance) : null);
      List<String> places = (List<String>) activityInstance.getTransientContextObject("places");
      places.add(place);
      activityInstance.onwards();
    }
    
    @Override
    public void validate(ActivityDefinition activityDefinition, Validator validator) {
      activityDefinition.initializeBindings(validator);
    }
  }
}

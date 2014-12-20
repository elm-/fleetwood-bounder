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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.WorkflowEngineConfiguration;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.plugin.AbstractActivityType;
import com.heisenberg.impl.plugin.Binding;
import com.heisenberg.impl.plugin.ConfigurationField;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Description;
import com.heisenberg.impl.plugin.DescriptorSender;
import com.heisenberg.impl.plugin.Descriptors;
import com.heisenberg.impl.plugin.Label;
import com.heisenberg.impl.plugin.ServiceRegistry;


/**
 * @author Walter White
 */
public class ActivityTypeDescriptorTest {
  
  public static final Logger log = LoggerFactory.getLogger(ActivityTypeDescriptorTest.class);

  @Test 
  public void testConfigurableActivityTypeDescriptor() {
    WorkflowEngineImpl processEngine = (WorkflowEngineImpl) new WorkflowEngineConfiguration()
      .registerActivityType(new MyConfigurableActivityType())
      .registerActivityType(new MyBindingActivityType())
      .buildProcessEngine();
    
    ServiceRegistry serviceRegistry = processEngine.getServiceRegistry();
    Descriptors descriptors = serviceRegistry.getService(Descriptors.class);
    JsonService jsonService = serviceRegistry.getService(JsonService.class);
    new DescriptorSender(descriptors, jsonService)
      .organizationKey("myorg")
      .send();
//
//    JsonService jsonService = processEngine.getJsonService();
//    log.debug("Activity type descriptors:");
//    log.debug(jsonService.objectToJsonStringPretty(processEngine.activityTypeService.descriptors));
//    log.debug("Data type descriptors:");
//    log.debug(jsonService.objectToJsonStringPretty(processEngine.dataTypeService.descriptors));
  }
  
  @JsonTypeName("myBindingActivityTypeId")
  @Label("My dynamically configurable activity")
  @Description("Dynamic configuration means that the value used at runtime is derived from process variable data")
  public static class MyBindingActivityType extends AbstractActivityType {
    @ConfigurationField
    @Label("Place")
    Binding<String> place;
    public MyBindingActivityType() {
    }
    @Override
    public void start(ControllableActivityInstance activityInstance) {
    }
  }
  
  @JsonTypeName("myConfigurableActivityTypeId")
  @Label("My configurable activity")
  @Description("Records the configuration in a static member field")
  public static class MyConfigurableActivityType extends AbstractActivityType {
    @ConfigurationField(required=true)
    @Label("Configuration property label")
    String configuration;
    public MyConfigurableActivityType() {
    }
    @Override
    public void start(ControllableActivityInstance activityInstance) {
    }
  }
}

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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.test.activitytype.ActivityTypeBindingConfigurationTest.MyBindingActivityType;
import com.heisenberg.test.activitytype.ActivityTypeStaticConfigurationTest.MyConfigurableActivityType;


/**
 * @author Walter White
 */
public class ActivityTypeDescriptorTest {
  
  public static final Logger log = LoggerFactory.getLogger(ActivityTypeDescriptorTest.class);

  @Test 
  public void testConfigurableActivityTypeDescriptor() {
    ProcessEngineImpl processEngine = (ProcessEngineImpl) new MemoryProcessEngineConfiguration()
      .registerConfigurableActivityType(new MyConfigurableActivityType())
      .registerConfigurableActivityType(new MyBindingActivityType())
      .buildProcessEngine();

    JsonService jsonService = processEngine.getJsonService();
    log.debug("Activity type descriptors:");
    log.debug(jsonService.objectToJsonStringPretty(processEngine.activityTypes.descriptors));
    log.debug("Data type descriptors:");
    log.debug(jsonService.objectToJsonStringPretty(processEngine.dataTypes.descriptors));
  }

}

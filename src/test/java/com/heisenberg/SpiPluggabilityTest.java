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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.form.Form;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.Binding;
import com.heisenberg.spi.Label;
import com.heisenberg.spi.SpiDescriptor;
import com.heisenberg.type.TextType;


/**
 * @author Walter White
 */
public class SpiPluggabilityTest {
  
  public static final Logger log = LoggerFactory.getLogger(SpiPluggabilityTest.class);

  @Test
  public void testOne() {
    Json json = new Json();
    SpiDescriptor spiDescriptor = new SpiDescriptor(MyCustomType.class);
    log.debug("From oss on-premise to SaaS process builder:");
    log.debug(json.objectToJsonStringPretty(spiDescriptor)+"\n");
    
    log.debug("SaaS process builder shows the activity in the pallete");

    Form configurationForm = spiDescriptor.getConfigurationForm();
    log.debug("SaaS process builder shows following configuration form:");
    log.debug(json.objectToJsonStringPretty(configurationForm)+"\n");
    
    configurationForm.fields.get(0).value = "hela";
    Binding<TextType> binding = new Binding<>();
    binding.variableName = "v"; 
    configurationForm.fields.get(1).value = binding;
    
    log.debug("SaaS process sends following configuration form back as part of the process that is exported:");
    String configurationFormText = json.objectToJsonStringPretty(configurationForm);
    log.debug(configurationFormText+"\n");
    
    Form configurationFormInstance = json.jsonToObject(configurationFormText, Form.class);
    // json parsing post processing
    configurationFormInstance.parseValues(configurationForm);
    
    MyCustomType myCustomType = (MyCustomType) spiDescriptor.instantiateAndConfigure(configurationFormInstance);
    myCustomType.start(null);
  }

  public static class MyCustomType extends ActivityType {
    private static final String ID = "mca";

    @Override
    public String getLabel() {
      return "SAP mybapi";
    }
    @Override
    public String getId() {
      return ID;
    }
    
    @Label("Function name")
    String functionName;

    @Label("Parameter one")
    Binding<TextType> parameterOne; 
    
    @Override
    public void start(ActivityInstanceImpl activityInstance) {
      log.debug("Function name: "+functionName);
      log.debug("Parameter one: "+parameterOne.getValue(activityInstance, String.class));
    }
  }
}

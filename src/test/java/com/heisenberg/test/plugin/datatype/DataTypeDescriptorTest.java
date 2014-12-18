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
package com.heisenberg.test.plugin.datatype;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.ProcessEngineConfiguration;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.type.AbstractDataType;
import com.heisenberg.impl.type.InvalidValueException;
import com.heisenberg.plugin.activities.ConfigurationField;
import com.heisenberg.plugin.activities.Label;
import com.heisenberg.test.plugin.activitytype.ActivityTypeDescriptorTest;


/**
 * @author Walter White
 */
public class DataTypeDescriptorTest {

  public static final Logger log = LoggerFactory.getLogger(ActivityTypeDescriptorTest.class);

  @Test 
  public void testDataTypeDescriptors() {
    ProcessEngineImpl processEngine = (ProcessEngineImpl) new ProcessEngineConfiguration()
      .registerJavaBeanType(Money.class)
      .registerConfigurableDataType(new InvoiceType())
      .buildProcessEngine();

    JsonService jsonService = processEngine.getJsonService();
    log.debug("Data type descriptors:");
    log.debug(jsonService.objectToJsonStringPretty(processEngine.dataTypeService.descriptors));
  }

  static class Money {
    public int amount;
    public String currency;
    public Money() {
    }
    public Money(int amount, String currency) {
      this.amount = amount;
      this.currency = currency;
    }
  }

  @JsonTypeName("invoice")
  static class InvoiceType extends AbstractDataType {
    @ConfigurationField
    @Label("Include lines?")
    public String includeLines;
    @Override
    public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
      return null;
    }
    
  }
}

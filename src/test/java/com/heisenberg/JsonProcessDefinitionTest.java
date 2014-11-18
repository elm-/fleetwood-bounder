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

import com.heisenberg.definition.OrganizationId;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.ProcessId;
import com.heisenberg.definition.UserId;
import com.heisenberg.json.Json;
import com.heisenberg.spi.Type;
import com.heisenberg.type.ChoiceType;
import com.heisenberg.util.Time;


/**
 * @author Walter White
 */
public class JsonProcessDefinitionTest {
  
  public static final Logger log = LoggerFactory.getLogger(JsonProcessDefinitionTest.class);

  @Test
  public void testProcessDefinitionJson() {
    ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl()
    .deployedUserId(new UserId("me"))
    .deployedTime(Time.now())
    .organizationId(new OrganizationId("myorg"))
    .processId(new ProcessId("myprocess"))
    .version(6l)
    .line(1l)
    .column(1l)
    .type(new ChoiceType()
      .id("country")
      .label("Country")
      .option("be", "Belgium")
      .option("de", "Germany")
      .option("fr", "France")
      .option("uk", "UK")
      .option("us", "US")
    );
    
    processDefinition.newVariable()
      .name("t")
      .initialValue("iv")
      .type(Type.TEXT);
    
    processDefinition.newActivity()
      .activityTypeId(Go.ID)
      .line(20l)
      .column(30l)
      .parameterValue(Go.PLACE, "Antwerp")
      .name("go");
    
    processDefinition.newTransition()
      .from("wait1")
      .to("wait2");
    
    Json json = new Json();
    
    String processDefinitionJsonText = json.objectToJsonStringPretty(processDefinition);

    log.debug(processDefinitionJsonText);
  }
}

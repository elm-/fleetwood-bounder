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
package com.heisenberg.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.type.ChoiceType;
import com.heisenberg.api.type.TextType;
import com.heisenberg.api.util.OrganizationId;
import com.heisenberg.api.util.ProcessId;
import com.heisenberg.api.util.UserId;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.json.Json;

/**
 * @author Walter White
 */
public class JsonProcessDefinitionTest {
  
  public static final Logger log = LoggerFactory.getLogger(JsonProcessDefinitionTest.class);

  @Test
  public void testProcessDefinitionJson() {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class);
  
    processEngine.json.objectMapper.registerSubtypes(new NamedType(Go.class, "go"));

    ProcessBuilder process = processEngine.newProcess();
    process.deployedUserId(new UserId("me"))
    .deployedTime(Time.now())
    .organizationId(new OrganizationId("myorg"))
    .processId(new ProcessId("myprocess"))
    .version(6l)
    .line(1l)
    .column(2l)
    .dataType(new ChoiceType()
      .id("country")
      .label("Country")
      .option("be", "Belgium")
      .option("de", "Germany")
      .option("fr", "France")
      .option("uk", "UK")
      .option("us", "US"));
    
    process.newVariable()
      .id("t")
      .initialValueJson("iv")
      .dataType(TextType.INSTANCE)
      .line(3l)
      
      .column(4l);
    
    process.newActivity()
      .activityType(new Go())
      .line(20l)
      .column(30l)
      .id("one");

    process.newActivity()
      .activityType(new Go())
      .id("two");

    process.newTransition()
      .from("one")
      .to("two");
    
    processEngine.deployProcessDefinition(process);
    
    Json json = processEngine.json;
    
    String processDefinitionJsonText = json.objectToJsonStringPretty(process);

    log.debug(processDefinitionJsonText);
    
    ProcessDefinitionImpl processDefinition = json.jsonToObject(processDefinitionJsonText, ProcessDefinitionImpl.class);
    assertNotNull(processDefinition);
    assertEquals("myorg", processDefinition.organizationId.getInternal());
    ChoiceType choiceType = (ChoiceType) processDefinition.dataTypesMap.get("country");
    assertEquals("Belgium", choiceType.getOptions().get("be"));
    ActivityDefinitionImpl one = processDefinition.getActivityDefinition("one");
    assertEquals("one", one.id.getInternal());
    assertEquals(Go.class, one.activityType.getClass());
  }
}

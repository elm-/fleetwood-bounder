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

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.Type;
import com.heisenberg.type.ChoiceType;
import com.heisenberg.util.Time;
import com.heisenberg.api.definition.ProcessBuilder;
import com.heisenberg.api.id.OrganizationId;
import com.heisenberg.api.id.ProcessId;
import com.heisenberg.api.id.UserId;

/**
 * @author Walter White
 */
public class JsonProcessDefinitionTest {
  
  public static final Logger log = LoggerFactory.getLogger(JsonProcessDefinitionTest.class);

  @Test
  public void testProcessDefinitionJson() {
    ProcessBuilder processBuilder = new ProcessDefinitionImpl();
    processBuilder.deployedUserId(new UserId("me"))
    .deployedTime(Time.now())
    .organizationId(new OrganizationId("myorg"))
    .processId(new ProcessId("myprocess"))
    .version(6l)
    .line(1l)
    .column(2l)
    .type(new ChoiceType()
      .id("country")
      .label("Country")
      .option("be", "Belgium")
      .option("de", "Germany")
      .option("fr", "France")
      .option("uk", "UK")
      .option("us", "US")
    );
    
    processBuilder.newVariable()
      .name("t")
      .initialValue("iv")
      .type(Type.TEXT)
      .line(3l)
      
      .column(4l);
    
    processBuilder.newActivity()
      .activityType(new Go())
      .line(20l)
      .column(30l)
      .name("one");

    processBuilder.newActivity()
      .activityType(new Go())
      .name("two");

    processBuilder.newTransition()
      .from("one")
      .to("two");
    
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class);
    
    Json json = processEngine.json;
    
    String processDefinitionJsonText = json.objectToJsonStringPretty(processBuilder);

    log.debug(processDefinitionJsonText);
    
    ProcessDefinitionImpl processDefinition = json.jsonToObject(processDefinitionJsonText, ProcessDefinitionImpl.class);
    assertNotNull(processDefinition);
    assertEquals("myorg", processDefinition.organizationId.getInternal());
    ChoiceType choiceType = (ChoiceType) processDefinition.typesMap.get("country");
    assertEquals("Belgium", choiceType.getOptions().get("be"));
    ActivityDefinitionImpl one = processDefinition.getActivityDefinition("one");
    assertEquals("one", one.name);
    assertEquals(Go.class, one.activityType.getClass());
  }
}

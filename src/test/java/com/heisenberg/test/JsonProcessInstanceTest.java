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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.type.TextType;
import com.heisenberg.api.util.ActivityInstanceId;
import com.heisenberg.api.util.ProcessDefinitionId;
import com.heisenberg.api.util.ProcessInstanceId;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.engine.memory.MemoryProcessEngine;
import com.heisenberg.impl.engine.operation.StartActivityInstanceOperation;
import com.heisenberg.impl.engine.operation.NotifyEndOperation;
import com.heisenberg.impl.engine.updates.ActivityInstanceCreateUpdate;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.impl.json.Json;

/**
 * @author Walter White
 */
public class JsonProcessInstanceTest {
  
  public static final Logger log = LoggerFactory.getLogger(JsonProcessInstanceTest.class);

  @Test
  public void testProcessInstanceJson() {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerActivityType(Go.class);
    
    ProcessDefinitionImpl p = new ProcessDefinitionImpl();
    p.id = new ProcessDefinitionId("pdid");
    
    VariableDefinitionImpl v = (VariableDefinitionImpl) p.newVariable()
      .id("v")
      .dataType(TextType.INSTANCE);
    
    ActivityDefinitionImpl a = (ActivityDefinitionImpl) p.newActivity()
      .activityType(new Go())
      .id("one");
  
    p.newActivity()
      .activityType(new Go())
      .id("two");
    
    ProcessInstanceImpl processInstance = new ProcessInstanceImpl();
    processInstance.processInstance = processInstance;
    processInstance.processEngine = processEngine;
    processInstance.processDefinition = p;
    processInstance.id = new ProcessInstanceId("pid");
    processInstance.start = Time.now().minusMillis(10);
    processInstance.end = Time.now().minusMillis(5);
    processInstance.duration = 45l;
    processInstance.lock = new LockImpl();
    processInstance.lock.owner = "you";
    processInstance.lock.time = Time.now();
    processInstance.isAsync = true;

    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.processEngine = processEngine;
    activityInstance.processInstance = processInstance;
    activityInstance.parent = processInstance;
    activityInstance.id = new ActivityInstanceId("aid");
    activityInstance.start = Time.now().plusMillis(5);
    activityInstance.end = Time.now().plusMillis(10);
    activityInstance.duration = 45l;
    activityInstance.activityDefinition = a;

    processInstance.activityInstances = new ArrayList<>();
    processInstance.activityInstances.add(activityInstance);

    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.processEngine = processEngine;
    variableInstance.processInstance = processInstance;
    variableInstance.parent = activityInstance;
    variableInstance.variableDefinition = v;
    variableInstance.value = "hello";

    processInstance.variableInstances = new ArrayList<>();
    processInstance.variableInstances.add(variableInstance);

    processInstance.operations = new LinkedList<>();
    processInstance.operations.add(new StartActivityInstanceOperation(activityInstance));
    
    processInstance.asyncOperations = new LinkedList<>();
    processInstance.operations.add(new NotifyEndOperation(activityInstance));
    
    processInstance.updates = new ArrayList<>();
    processInstance.updates.add(new ActivityInstanceCreateUpdate(activityInstance));

    Json json = processEngine.json;
    
    String processInstanceJsonText = json.objectToJsonStringPretty(processInstance);

    log.debug(processInstanceJsonText);
    
    processInstance = json.jsonToObject(processInstanceJsonText, ProcessInstanceImpl.class);
    assertNotNull(processInstance);
//    assertEquals("myorg", processDefinition.organizationId.getInternal());
//    ChoiceType choiceType = (ChoiceType) processDefinition.typesMap.get("country");
//    assertEquals("Belgium", choiceType.getOptions().get("be"));
//    ActivityDefinitionImpl one = processDefinition.getActivityDefinition("one");
//    assertEquals("one", one.name);
//    assertEquals(Go.class, one.activityType.getClass());
  }
}

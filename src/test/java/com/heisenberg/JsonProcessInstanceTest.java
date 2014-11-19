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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.id.ActivityInstanceId;
import com.heisenberg.api.id.ProcessDefinitionId;
import com.heisenberg.api.id.ProcessInstanceId;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.VariableDefinitionImpl;
import com.heisenberg.engine.memory.MemoryProcessEngine;
import com.heisenberg.engine.operation.ActivityInstanceStartOperation;
import com.heisenberg.engine.operation.NotifyActivityInstanceEndToParent;
import com.heisenberg.engine.updates.ActivityInstanceCreateUpdate;
import com.heisenberg.engine.updates.ActivityInstanceEndUpdate;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.LockImpl;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.instance.VariableInstanceImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.Type;
import com.heisenberg.util.Time;

/**
 * @author Walter White
 */
public class JsonProcessInstanceTest {
  
  public static final Logger log = LoggerFactory.getLogger(JsonProcessInstanceTest.class);

  @Test
  public void testProcessInstanceJson() {
    ProcessEngineImpl processEngine = new MemoryProcessEngine()
      .registerActivityType(new Go());
    
    ProcessDefinitionImpl p = new ProcessDefinitionImpl();
    p.id = new ProcessDefinitionId("pdid");
    
    VariableDefinitionImpl v = (VariableDefinitionImpl) p.newVariable()
      .name("v")
      .type(Type.TEXT);
    
    ActivityDefinitionImpl a = (ActivityDefinitionImpl) p.newActivity()
      .activityTypeId(Go.ID)
      .name("one");
  
    p.newActivity()
      .activityTypeId(Go.ID)
      .name("two");
    
    ProcessInstanceImpl processInstance = new ProcessInstanceImpl();
    processInstance.processInstance = processInstance;
    processInstance.processEngine = processEngine;
    processInstance.processDefinition = p;
    processInstance.id = new ProcessInstanceId("pid");
    processInstance.start = Time.now()-10;
    processInstance.end = Time.now()-5;
    processInstance.duration = 45l;
    processInstance.lock = new LockImpl();
    processInstance.lock.owner = "you";
    processInstance.lock.time = 928734598475l;
    processInstance.isAsync = true;

    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.processEngine = processEngine;
    activityInstance.processInstance = processInstance;
    activityInstance.parent = processInstance;
    activityInstance.id = new ActivityInstanceId("aid");
    activityInstance.start = Time.now()+5;
    activityInstance.end = Time.now()+10;
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
    processInstance.operations.add(new ActivityInstanceStartOperation(activityInstance));
    
    processInstance.asyncOperations = new LinkedList<>();
    processInstance.operations.add(new NotifyActivityInstanceEndToParent(activityInstance));
    
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

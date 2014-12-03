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

import static com.heisenberg.test.TestHelper.assertActivityInstancesOpen;
import static com.heisenberg.test.TestHelper.findActivityInstanceOpen;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.api.ActivityInstanceMessageBuilder;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.ProcessInstanceBuilder;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.activities.bpmn.EmbeddedSubprocess;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.ScriptTask;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class MongoProcessEngineTest {
  
  public static final Logger log = LoggerFactory.getLogger(MongoProcessEngineTest.class);
  
  @Test
  public void testMongoProcessEngine() {
    ProcessEngine processEngine = new MongoProcessEngineConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine();
    
    ProcessDefinitionBuilder process = createProcess(processEngine);

    String processDefinitionId = processEngine
        .deployProcessDefinition(process)
        .checkNoErrorsAndNoWarnings()
        .getProcessDefinitionId();
      
    ProcessInstance processInstance = processEngine.newProcessInstance()
      .processDefinitionId(processDefinitionId));
    
    assertActivityInstancesOpen(processInstance, "sub", "subTask");

    ActivityInstance subTaskInstance = findActivityInstanceOpen(processInstance, "subTask");
    
    processEngine.sendActivityInstanceMessage(new ActivityInstanceMessageBuilder()
      .activityInstanceId(subTaskInstance.getId())
    );
  }

  @Test
  public void testPrintProcessDefinitionJson() {
    ProcessEngine processEngine = new MongoProcessEngineConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine();
    
    ProcessDefinitionBuilder process = createProcess(processEngine);
    
    String processJsonPretty = ((ProcessEngineImpl)processEngine).jsonService.objectToJsonStringPretty(process);

    log.debug(processJsonPretty);
  }


  public static ProcessDefinitionBuilder createProcess(ProcessEngine processEngine) {
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
  
    process.newActivity()
      .activityType(StartEvent.INSTANCE)
      .id("start");

    process.newTransition()
      .from("start").to("scriptBefore");
    
    process.newActivity()
      .activityType(new ScriptTask())
      .id("scriptBefore");

    process.newTransition()
      .from("scriptBefore").to("sub");

    ActivityBuilder embeddedSubprocess = process.newActivity()
      .activityType(EmbeddedSubprocess.INSTANCE)
      .id("sub");

    embeddedSubprocess.newActivity()
      .activityType(StartEvent.INSTANCE)
      .id("subStart");

    embeddedSubprocess.newTransition()
      .from("subStart").to("subScript");

    embeddedSubprocess.newActivity()
      .activityType(new ScriptTask())
      .id("subScript");

    embeddedSubprocess.newTransition()
      .from("subScript").to("subTask");

    embeddedSubprocess.newActivity()
      .activityType(new UserTask())
      .id("subTask");

    embeddedSubprocess.newTransition()
      .from("subTask").to("subEnd");

    embeddedSubprocess.newActivity()
      .activityType(EndEvent.INSTANCE)
      .id("subEnd");

    process.newTransition()
      .from("sub").to("scriptAfter");

    process.newActivity()
      .activityType(new ScriptTask())
      .id("scriptAfter");

    process.newTransition()
      .from("scriptAfter").to("end");

    process.newActivity()
      .activityType(EndEvent.INSTANCE)
      .id("end");
    return process;
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

    @Override
    public String getType() {
      return "testGo";
    }
  }
}

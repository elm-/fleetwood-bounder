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
package com.heisenberg.test.mongo;

import static com.heisenberg.test.TestHelper.assertOpen;
import static com.heisenberg.test.TestHelper.findActivityInstanceOpen;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.activitytypes.EmbeddedSubprocess;
import com.heisenberg.api.activitytypes.EndEvent;
import com.heisenberg.api.activitytypes.ScriptTask;
import com.heisenberg.api.activitytypes.StartEvent;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.plugin.AbstractActivityType;
import com.heisenberg.impl.plugin.Binding;
import com.heisenberg.impl.plugin.ConfigurationField;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Label;
import com.heisenberg.mongo.MongoWorkflowEngineConfiguration;
import com.heisenberg.test.TestExecutorService;


/**
 * @author Walter White
 */
public class MongoWorkflowEngineTest {
  
  public static final Logger log = LoggerFactory.getLogger(MongoWorkflowEngineTest.class);
  
  @Test
  public void testMongoProcessEngine() {
    WorkflowEngine workflowEngine = new MongoWorkflowEngineConfiguration()
      .server("localhost", 27017)
      .registerService(new TestExecutorService())
      .buildProcessEngine();
    
    WorkflowBuilder process = createProcess(workflowEngine);

    String processDefinitionId = process.deploy()
        .checkNoErrorsAndNoWarnings()
        .getWorkflowId();
      
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();
    
    assertOpen(workflowInstance, "sub", "subTask");

    ActivityInstance subTaskInstance = findActivityInstanceOpen(workflowInstance, "subTask");
    
    workflowEngine.newMessage()
      .activityInstanceId(subTaskInstance.getId())
      .send();
  }

  public static WorkflowBuilder createProcess(WorkflowEngine workflowEngine) {
    WorkflowBuilder process = workflowEngine.newWorkflow()
      .name("load");
  
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
  
  @JsonTypeName("auto")
  public static class Auto extends AbstractActivityType {
    @ConfigurationField()
    @Label("Place")
    Binding<String> placeBinding;

    public Auto placeBinding(Binding<String> placeBinding) {
      this.placeBinding = placeBinding;
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(ControllableActivityInstance activityInstance) {
      String place = (placeBinding!=null ? activityInstance.getValue(placeBinding) : null);
      List<String> places = (List<String>) activityInstance.getTransientContextObject("places");
      places.add(place);
      activityInstance.onwards();
    }
//    @Override
//    public void validate(ActivityDefinition activityDefinition, Validator validator) {
//      activityDefinition.validateConfigurationFields(validator);
//    }
  }
}

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
package com.heisenberg.load;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.EmbeddedSubprocess;
import com.heisenberg.api.activities.bpmn.EndEvent;
import com.heisenberg.api.activities.bpmn.ScriptTask;
import com.heisenberg.api.activities.bpmn.StartEvent;
import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.mongo.MongoProcessEngineConfiguration;


/**
 * @author Walter White
 */
public class DeployLoadProcess {

  public static void main(String[] args) {
    ProcessEngineImpl processEngine = (ProcessEngineImpl) new MongoProcessEngineConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine();
    
    createProcess(processEngine)
      .deploy();
  }

  public static ProcessDefinitionBuilder createProcess(ProcessEngine processEngine) {
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition()
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
}

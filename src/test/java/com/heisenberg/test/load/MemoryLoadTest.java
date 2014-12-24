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
package com.heisenberg.test.load;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.activitytypes.DefaultTask;
import com.heisenberg.api.activitytypes.EndEvent;
import com.heisenberg.api.activitytypes.StartEvent;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.test.TestMemoryWorkflowEngine;
import com.heisenberg.test.mongo.MongoWorkflowEngineTest;

/**
 * @author Walter White
 */
@Ignore
public class MemoryLoadTest  {
  
  static {
    System.setProperty("logback.configurationFile", "logback-load.xml");
  }
  
  public static final Logger log = LoggerFactory.getLogger(MemoryLoadTest.class);
  
  @Test
  public void test() {
    WorkflowEngine workflowEngine = new TestMemoryWorkflowEngine();
    
    String workflowId = MongoWorkflowEngineTest.createProcess(workflowEngine)
            .deploy();
//  String workflowId = createWorkflow(workflowEngine);

    runProcessInstance(workflowEngine, workflowId);
    
    long nbrOfThreads = 4;
    long processExecutionsPerThread = 50000;
    long processExecutions = nbrOfThreads*processExecutionsPerThread;
    
    // warm up
    for (int i=0; i<500; i++) {
      runProcessInstance(workflowEngine, workflowId);
    }

    // start measuring
    long start = System.currentTimeMillis();

    List<Thread> threads = new ArrayList<>();
    for (int i=0; i<nbrOfThreads; i++) {
      ProcessInstanceRunner thread = new ProcessInstanceRunner(workflowEngine, workflowId, processExecutionsPerThread);
      threads.add(thread);
    }
    
    for (Thread thread: threads) {
      thread.start();
    }

    for (Thread thread: threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    long end = System.currentTimeMillis();
    log.info(processExecutions+" process executions in "+((end-start)/1000f)+ " seconds");
    log.info(processExecutions+" process executions at "+((processExecutions*1000f)/(float)(end-start))+ " per second");
  }

  public String createWorkflow(WorkflowEngine workflowEngine) {
    WorkflowBuilder w = workflowEngine.newWorkflow()
      .name("load");
  
    w.newActivity()
      .activityType(StartEvent.INSTANCE)
      .id("start");

    w.newTransition()
      .from("start").to("s");

    w.newActivity()
      .activityType(new DefaultTask())
      .id("s");

    w.newTransition()
      .from("s").to("end");

    w.newActivity()
      .activityType(EndEvent.INSTANCE)
      .id("end");
    
    return w.deploy();
  }

  class ProcessInstanceRunner extends Thread {
    WorkflowEngine workflowEngine;
    String processDefinitionId;
    long processExecutions;
    public ProcessInstanceRunner(WorkflowEngine workflowEngine, String processDefinitionId, long processExecutions) {
      this.workflowEngine = workflowEngine;
      this.processDefinitionId = processDefinitionId;
      this.processExecutions = processExecutions;
    }

    @Override
    public void run() {
      for (int i=0; i<processExecutions; i++) {
        runProcessInstance(workflowEngine, processDefinitionId);
      }
    }
  }

  void runProcessInstance(WorkflowEngine workflowEngine, String workflowId) {
    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(workflowId)
      .startWorkflowInstance();
    
    String subTaskInstanceId = workflowInstance
            .findActivityInstanceByActivityId("subTask")
            .getId();
  
    workflowInstance = workflowEngine.newMessage()
      .processInstanceId(workflowInstance.getId())
      .activityInstanceId(subTaskInstanceId)
      .send();
    
    assertTrue(workflowInstance.isEnded());
  }
}

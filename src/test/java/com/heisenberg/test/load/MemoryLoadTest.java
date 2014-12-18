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

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.memory.MemoryProcessEngine;
import com.heisenberg.test.mongo.MongoProcessEngineTest;

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
    ProcessEngine processEngine = new MemoryProcessEngine();
    String processDefinitionId = MongoProcessEngineTest.createProcess(processEngine)
            .deploy()
            .getProcessDefinitionId();
    
    long nbrOfThreads = 4;
    long processExecutionsPerThread = 50000;
    long processExecutions = nbrOfThreads*processExecutionsPerThread;
    
    // warm up
    for (int i=0; i<500; i++) {
      runProcessInstance(processEngine, processDefinitionId);
    }

    // start measuring
    long start = System.currentTimeMillis();

    List<Thread> threads = new ArrayList<>();
    for (int i=0; i<nbrOfThreads; i++) {
      ProcessInstanceRunner thread = new ProcessInstanceRunner(processEngine, processDefinitionId, processExecutionsPerThread);
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

  class ProcessInstanceRunner extends Thread {
    ProcessEngine processEngine;
    String processDefinitionId;
    long processExecutions;
    public ProcessInstanceRunner(ProcessEngine processEngine, String processDefinitionId, long processExecutions) {
      this.processEngine = processEngine;
      this.processDefinitionId = processDefinitionId;
      this.processExecutions = processExecutions;
    }

    @Override
    public void run() {
      for (int i=0; i<processExecutions; i++) {
        runProcessInstance(processEngine, processDefinitionId);
      }
    }
  }

  void runProcessInstance(ProcessEngine processEngine, String processDefinitionId) {
    ProcessInstance processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance();
    
    String subTaskInstanceId = processInstance
            .findActivityInstanceByActivityDefinitionId("subTask")
            .getId();
  
    processInstance = processEngine.newMessage()
      .processInstanceId(processInstance.getId())
      .activityInstanceId(subTaskInstanceId)
      .send();
    
    assertTrue(processInstance.isEnded());
  }
}

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.impl.MessageImpl;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.StartBuilderImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionSerializer;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.rest.HeisenbergServer;
import com.heisenberg.rest.ObjectMapperProvider;
import com.heisenberg.test.db.MongoProcessEngineTest;

/**
 * @author Walter White
 */
@Ignore
public class LoadTest extends JerseyTest {
  
  static {
    System.setProperty("logback.configurationFile", "logback-load.xml");
  }
  
  public static final Logger log = LoggerFactory.getLogger(LoadTest.class);
  
  static MeasuringMongoProcessEngine processEngine = null;
  
  @Override
  protected Application configure() {
    return HeisenbergServer.buildRestApplication(getProcessEngine());
  }

  protected ProcessEngineImpl getProcessEngine() {
    if (processEngine!=null) {
      return processEngine;
    }
    processEngine = new MeasuringMongoProcessEngine(new MongoProcessEngineConfiguration()
//      .writeConcernInsertProcessDefinition(WriteConcern.UNACKNOWLEDGED)
//      .writeConcernFlushUpdates(WriteConcern.UNACKNOWLEDGED)
//      .writeConcernInsertProcessInstance(WriteConcern.UNACKNOWLEDGED)
      .server("localhost", 27017));
    return processEngine;
  }
  
  @Override
  protected void configureClient(ClientConfig clientConfig) {
    clientConfig.register(new ObjectMapperProvider(getProcessEngine()));
  }

  @Test
  public void test() {
    String processDefinitionId = deployProcessDefinition();
    
    long processExecutionsPerThread = 2000;
    long processExecutions = 4*processExecutionsPerThread;
    
    for (int i=0; i<20; i++) {
      runProcessInstance(processDefinitionId);
    }

    long testStartMillis = System.currentTimeMillis();
    long start = System.currentTimeMillis();
    
    Thread t1 = new ProcessInstanceRunner(processDefinitionId, processExecutionsPerThread);
    Thread t2 = new ProcessInstanceRunner(processDefinitionId, processExecutionsPerThread);
    Thread t3 = new ProcessInstanceRunner(processDefinitionId, processExecutionsPerThread);
    Thread t4 = new ProcessInstanceRunner(processDefinitionId, processExecutionsPerThread);

    t1.start();
    t2.start();
    t3.start();
    t4.start();
    
    try {
      t1.join();
      t2.join();
      t3.join();
      t4.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    long end = System.currentTimeMillis();
    log.info(processExecutions+" process executions in "+((end-start)/1000f)+ " seconds");
    log.info(processExecutions+" process executions at "+((processExecutions*1000f)/(float)(end-start))+ " per second");
    
    processEngine.logReport(1000, testStartMillis);
  }

  class ProcessInstanceRunner extends Thread {
    String processDefinitionId;
    long processExecutions;
    public ProcessInstanceRunner(String processDefinitionId, long processExecutions) {
      this.processDefinitionId = processDefinitionId;
      this.processExecutions = processExecutions;
    }

    @Override
    public void run() {
      for (int i=0; i<processExecutions; i++) {
        runProcessInstance(processDefinitionId);
      }
    }
  }

  protected String deployProcessDefinition() {
    ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) MongoProcessEngineTest.createProcess(processEngine);
    processDefinition.visit(new ProcessDefinitionSerializer());
    DeployResult deployResponse = target("deploy").request()
            .post(Entity.entity(processDefinition, MediaType.APPLICATION_JSON))
            .readEntity(DeployResult.class);
    assertFalse(deployResponse.getIssueReport(), deployResponse.hasIssues());
    return deployResponse.getProcessDefinitionId();
  }

  void runProcessInstance(String... processDefinitionIds) {
    for (String processDefinitionId: processDefinitionIds) {
      StartBuilder startProcessInstanceRequest = new StartBuilderImpl()
        .processDefinitionId(processDefinitionId);
      
      ProcessInstanceImpl processInstance = target("start").request()
              .post(Entity.entity(startProcessInstanceRequest, MediaType.APPLICATION_JSON))
              .readEntity(ProcessInstanceImpl.class);
  
      String subTaskInstanceId = processInstance
              .findActivityInstanceByActivityDefinitionId("subTask")
              .getId();
  
      MessageBuilder notifyActivityInstanceRequest = new MessageImpl()
        .processInstanceId(processInstance.id)
        .activityInstanceId(subTaskInstanceId);
      
      processInstance = target("message").request()
              .post(Entity.entity(notifyActivityInstanceRequest, MediaType.APPLICATION_JSON))
              .readEntity(ProcessInstanceImpl.class);
  
      log.debug("response: " + processEngine.jsonService.objectToJsonStringPretty(processInstance));
      assertTrue(processInstance.isEnded());
    }
  }
}

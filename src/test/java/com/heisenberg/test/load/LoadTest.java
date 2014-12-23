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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.impl.MessageImpl;
import com.heisenberg.impl.StartImpl;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.mongo.MongoWorkflowEngineConfiguration;
import com.heisenberg.server.ObjectMapperResolver;
import com.heisenberg.server.WorkflowServer;
import com.heisenberg.test.mongo.MongoWorkflowEngineTest;

/**
 * @author Walter White
 */
@Ignore
public class LoadTest extends JerseyTest {
  
  static {
    System.setProperty("logback.configurationFile", "logback-load.xml");
  }
  
  public static final Logger log = LoggerFactory.getLogger(LoadTest.class);
  
  static MeasuringMongoWorkflowEngine workflowEngine = null;
  
  @Override
  protected Application configure() {
    return WorkflowServer.buildRestApplication(getWorkflowEngine());
  }

  protected WorkflowEngineImpl getWorkflowEngine() {
    if (workflowEngine!=null) {
      return workflowEngine;
    }
    workflowEngine = new MeasuringMongoWorkflowEngine(new MongoWorkflowEngineConfiguration()
    .server("localhost", 27017));
    return workflowEngine;
  }
  
  @Override
  protected void configureClient(ClientConfig clientConfig) {
    ObjectMapper objectMapper = getWorkflowEngine().getServiceRegistry().getService(ObjectMapper.class);
    clientConfig.register(new ObjectMapperResolver(objectMapper));
  }

  @Test
  public void test() {
    String processDefinitionId = deployProcessDefinition();
    
    long processExecutionsPerThread = 200;
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
    
    workflowEngine.logReport(1000, testStartMillis);
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
    WorkflowImpl processDefinition = (WorkflowImpl) MongoWorkflowEngineTest.createProcess(workflowEngine);
    DeployResult deployResponse = target("deploy").request()
            .post(Entity.entity(processDefinition, MediaType.APPLICATION_JSON))
            .readEntity(DeployResult.class);
    assertFalse(deployResponse.getIssueReport(), deployResponse.hasIssues());
    return deployResponse.getWorkflowId();
  }

  void runProcessInstance(String... processDefinitionIds) {
    for (String processDefinitionId: processDefinitionIds) {
      StartBuilder startProcessInstanceRequest = new StartImpl()
        .workflowId(processDefinitionId);
      
      WorkflowInstanceImpl processInstance = target("start").request()
              .post(Entity.entity(startProcessInstanceRequest, MediaType.APPLICATION_JSON))
              .readEntity(WorkflowInstanceImpl.class);
  
      String subTaskInstanceId = processInstance
              .findActivityInstanceByActivityDefinitionId("subTask")
              .getId();
  
      MessageBuilder notifyActivityInstanceRequest = new MessageImpl()
        .processInstanceId(processInstance.id)
        .activityInstanceId(subTaskInstanceId);
      
      processInstance = target("message").request()
              .post(Entity.entity(notifyActivityInstanceRequest, MediaType.APPLICATION_JSON))
              .readEntity(WorkflowInstanceImpl.class);
  
      log.debug("response: " + workflowEngine.jsonService.objectToJsonStringPretty(processInstance));
      assertTrue(processInstance.isEnded());
    }
  }
}

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
package com.heisenberg.test.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.logging.LogManager;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.load.LoadHelper;
import com.heisenberg.mongo.MongoWorkflowEngineConfiguration;
import com.heisenberg.rest.HeisenbergServer;
import com.heisenberg.rest.ObjectMapperProvider;
import com.heisenberg.test.mongo.MongoProcessEngineTest;

/**
 * @author Walter White
 */
public class RestTest extends JerseyTest {
  
  static {
    try {
      final InputStream inputStream = RestTest.class.getResourceAsStream("/logging.properties");
      LogManager.getLogManager().readConfiguration(inputStream);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static final Logger log = LoggerFactory.getLogger(RestTest.class);
  
  static WorkflowEngineImpl processEngine = null;
  
  @Override
  protected Application configure() {
    return HeisenbergServer.buildRestApplication(getProcessEngine());
  }

  protected WorkflowEngineImpl getProcessEngine() {
    if (processEngine!=null) {
      return processEngine;
    }
    processEngine = (WorkflowEngineImpl) new MongoWorkflowEngineConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine();
    return processEngine;
  }
  
  @Override
  protected void configureClient(ClientConfig clientConfig) {
    clientConfig.register(new ObjectMapperProvider(getProcessEngine()));
  }

  @Test
  public void test() {
    WorkflowImpl processDefinition = (WorkflowImpl) MongoProcessEngineTest.createProcess(processEngine);
    DeployResult deployResponse = target("deploy").request()
            .post(Entity.entity(processDefinition, MediaType.APPLICATION_JSON))
            .readEntity(DeployResult.class);

    assertFalse(deployResponse.getIssueReport(), deployResponse.hasIssues());
    
    runProcessInstance();
//    for (int i=0; i<20; i++) {
//      runProcessInstance(processDefinitionId);
//    }
//    long start = System.currentTimeMillis();
//    for (int i=20; i<1000; i++) {
//      log.info("starting "+i);
//      runProcessInstance(processDefinitionId);
//    }
//    long end = System.currentTimeMillis();
//    log.info("1000 process instances in "+((end-start)/1000f)+ " seconds");
//    log.info("1000 process instances in "+(1000000f/(end-start))+ " per second");
  }

  protected void runProcessInstance() {
    StartBuilder startProcessInstanceRequest = processEngine.newStart()
      .processDefinitionName("load");
    
    WorkflowInstanceImpl processInstance = target("start").request()
            .post(Entity.entity(startProcessInstanceRequest, MediaType.APPLICATION_JSON))
            .readEntity(WorkflowInstanceImpl.class);

    String processInstanceJson = processEngine.jsonService.objectToJsonString(processInstance);
    
    log.debug("response json: " + processInstanceJson);
    processInstance = LoadHelper.parseJson(processInstanceJson);
    String processInstanceId = processInstance.getId();
    String subTaskInstanceId = processInstance.findActivityInstanceByActivityDefinitionId("subTask").getId();

    MessageBuilder message = processEngine.newMessage()
      .processInstanceId(processInstanceId)
      .activityInstanceId(subTaskInstanceId);
    
    processInstance = target("message").request()
            .post(Entity.entity(message, MediaType.APPLICATION_JSON))
            .readEntity(WorkflowInstanceImpl.class);

    log.debug("response: " + processEngine.jsonService.objectToJsonStringPretty(processInstance));
    assertTrue(processInstance.isEnded());
  }
}

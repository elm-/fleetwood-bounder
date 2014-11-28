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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.NotifyActivityInstanceRequest;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionSerializer;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.rest.HeisenbergServer;
import com.heisenberg.rest.ObjectMapperProvider;
import com.heisenberg.test.TestHelper;
import com.heisenberg.test.db.MongoProcessEngineTest;

/**
 * @author Walter White
 */
public class RestTest extends JerseyTest {
  
  public static final Logger log = LoggerFactory.getLogger(RestTest.class);
  
  static ProcessEngineImpl processEngine = null;
  
  @Override
  protected Application configure() {
    return HeisenbergServer.buildRestApplication(getProcessEngine());
  }

  protected ProcessEngineImpl getProcessEngine() {
    if (processEngine!=null) {
      return processEngine;
    }
    processEngine = new MongoConfiguration()
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
    ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) MongoProcessEngineTest.createProcess(processEngine);
    processDefinition.visit(new ProcessDefinitionSerializer());
    
    DeployProcessDefinitionResponse deployResponse = target("deploy").request()
            .post(Entity.entity(processDefinition, MediaType.APPLICATION_JSON))
            .readEntity(DeployProcessDefinitionResponse.class);

    assertFalse(deployResponse.getIssueReport(), deployResponse.hasIssues());
    
    Object processDefinitionId = deployResponse.getProcessDefinitionId();
    
    StartProcessInstanceRequest startProcessInstanceRequest = new StartProcessInstanceRequest()
      .processDefinitionId(processDefinitionId);
    
    ProcessInstanceImpl processInstance = target("start").request()
            .post(Entity.entity(startProcessInstanceRequest, MediaType.APPLICATION_JSON))
            .readEntity(ProcessInstanceImpl.class);

    ActivityInstance subTaskInstance = TestHelper.findActivityInstanceOpen(processInstance, "subTask");

    NotifyActivityInstanceRequest notifyActivityInstanceRequest = new NotifyActivityInstanceRequest()
      .activityInstanceId(subTaskInstance.getId());
    
    processInstance = target("notify").request()
            .post(Entity.entity(notifyActivityInstanceRequest, MediaType.APPLICATION_JSON))
            .readEntity(ProcessInstanceImpl.class);

    log.debug("response: " + processEngine.json.objectToJsonStringPretty(processInstance));
    assertTrue(processInstance.isEnded());
  }
}

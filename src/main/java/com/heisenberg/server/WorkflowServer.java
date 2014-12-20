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
package com.heisenberg.server;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.mongo.MongoWorkflowEngineConfiguration;


/**
 * @author Walter White
 */
public class WorkflowServer {

  // Note Jetty HTTP container does not support deployment on context paths 
  // other than root path ("/"). Non-root context path is ignored during deployment.
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowServer.class+".HTTP");
  
  String baseUrl = "http://localhost:9999/";
  WorkflowEngineImpl processEngine;
  
  public WorkflowServer(WorkflowEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public WorkflowServer baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public static void main(String[] args) {
    WorkflowEngineImpl processEngine = (WorkflowEngineImpl) new MongoWorkflowEngineConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine();
    WorkflowServer workflowServer = new WorkflowServer(processEngine);
    workflowServer.start();
  }

  public void start() {
    try {
      URI baseUri = new URI(baseUrl);
      ResourceConfig config = buildRestApplication(processEngine);
      Server server = JettyHttpContainerFactory.createServer(baseUri, config);
      server.start();
      log.info("Heisenberg service started.");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static ResourceConfig buildRestApplication(WorkflowEngineImpl processEngine) {
    ResourceConfig config = new ResourceConfig();

    config.registerInstances(
            new DeployResource(processEngine),
            new StartResource(processEngine),
            new MessageResource(processEngine),
            new PingResource() );

    ObjectMapper objectMapper = processEngine.getServiceRegistry().getService(ObjectMapper.class);

    config.registerInstances(
            new JacksonFeature(),
            new ObjectMapperResolver(objectMapper),
            new RequestLogger(),
            new DefaultExceptionMapper() );
    
    return config;
  }
}

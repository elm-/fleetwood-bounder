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
package com.heisenberg.rest;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class HeisenbergServer {

  // Note Jetty HTTP container does not support deployment on context paths 
  // other than root path ("/"). Non-root context path is ignored during deployment.
  
  public static final Logger log = LoggerFactory.getLogger(HeisenbergServer.class+".HTTP");
  
  String baseUrl = "http://localhost:9999/";
  ProcessEngineImpl processEngine;
  
  public HeisenbergServer(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public HeisenbergServer baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public static void main(String[] args) {
    ProcessEngineImpl processEngine = (ProcessEngineImpl) new MongoProcessEngineConfiguration()
      .server("localhost", 27017)
      .buildProcessEngine();
    HeisenbergServer heisenbergServer = new HeisenbergServer(processEngine);
    heisenbergServer.start();
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

  public static ResourceConfig buildRestApplication(ProcessEngineImpl processEngine) {
    ResourceConfig config = new ResourceConfig();
    
    config.registerInstances(
            new JacksonFeature(),
            new DeployResource(processEngine),
            new StartResource(processEngine),
            new MessageResource(processEngine),
            new PingResource(),
            new ObjectMapperProvider(processEngine),
            new RequestLogger(),
            new DefaultExceptionMapper()
            );
    return config;
  }
}

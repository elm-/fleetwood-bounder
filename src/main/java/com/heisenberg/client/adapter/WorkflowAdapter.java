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
package com.heisenberg.client.adapter;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.server.DefaultExceptionMapper;
import com.heisenberg.server.ObjectMapperResolver;
import com.heisenberg.server.RequestLogger;


/**
 * @author Walter White
 */
public class WorkflowAdapter {

  public static final Logger log = LoggerFactory.getLogger(WorkflowAdapter.class);
  
  Integer port;
  ObjectMapper objectMapper;
  ResourceConfig config;
  Server server;
  
  public WorkflowAdapter() {
    objectMapper = new ObjectMapper();
    config = new ResourceConfig();
    config.registerInstances(
            new JacksonFeature(),
            new ObjectMapperResolver(objectMapper),
            new RequestLogger(),
            new DefaultExceptionMapper() );
  }
  
  public WorkflowAdapter port(Integer port) {
    this.port = port;
    return this;
  }

  public WorkflowAdapter registerResource(Object resource) {
    config.registerInstances(resource);
    return this;
  }

  public WorkflowAdapter registerSubclass(Class<?> subclass) {
    objectMapper.registerSubtypes(subclass);
    return this;
  }

  public void start() {
    try {
      URI baseUri = new URI("http://localhost"+(port!=null ? ":"+port : "")+"/");
      server = JettyHttpContainerFactory.createServer(baseUri, config);
      server.start();
      log.info("Workflow adapter started.");
      
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public void stop() {
    try {
      server.stop();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public ResourceConfig getResourceConfig() {
    return config;
  }
}

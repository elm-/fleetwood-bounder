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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.StartImpl;


/**
 * @author Walter White
 */
@Path("/start")
public class StartResource {
  
  public static final Logger log = LoggerFactory.getLogger(StartResource.class);
  
  WorkflowEngineImpl processEngine;
  
  public StartResource(WorkflowEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public WorkflowInstance startProcessInstance(StartImpl processInstanceBuilder) {
    return processEngine.startProcessInstance(processInstanceBuilder);
  }
}

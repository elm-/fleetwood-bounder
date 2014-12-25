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
package com.heisenberg.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.DataTypes;
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.builder.WorkflowQuery;
import com.heisenberg.api.builder.WorkflowInstanceQuery;
import com.heisenberg.api.builder.StartBuilder;


/**
 * @author Walter White
 */
public class ClientWorkflowEngine implements WorkflowEngine {
  
  public static final Logger log = LoggerFactory.getLogger(ClientWorkflowEngine.class);

  protected String baseUrl;
  
  public ClientWorkflowEngine(ClientWorkflowEngineConfiguration configuration) {
    this.baseUrl = configuration.getBaseUrl();
  }
  
//  @Override
//  public DeployResult deployProcessDefinition(ProcessDefinitionBuilder process) {
//    if (log.isDebugEnabled())
//      log.debug("HTTP POST /deploy");
//    if (log.isDebugEnabled())
//      log.debug("HTTP "+jsonService.objectToJsonStringPretty(process));
//    return null;
//  }
//
//  @Override
//  public ProcessInstance startWorkflowInstance(StartBuilderImpl processInstance) {
//    if (log.isDebugEnabled())
//      log.debug("HTTP POST /start");
//    if (log.isDebugEnabled())
//      log.debug("HTTP "+jsonService.objectToJsonStringPretty(processInstance));
//    return null;
//  }


  @Override
  public WorkflowBuilder newWorkflow() {
    return null;
  }

  @Override
  public StartBuilder newStart() {
    return null;
  }

  @Override
  public MessageBuilder newMessage() {
    return null;
  }

  @Override
  public WorkflowInstanceQuery newWorkflowInstanceQuery() {
    return null;
  }

  @Override
  public WorkflowQuery newWorkflowQuery() {
    return null;
  }

  @Override
  public DataTypes getDataTypes() {
    return null;
  }

  @Override
  public void deleteWorkflowInstance(String workflowInstanceId) {
  }

  @Override
  public void deleteWorkflow(String workflowId) {
  }
}

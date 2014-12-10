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
package com.heisenberg.impl.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ClientProcessEngineConfiguration;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.api.configuration.TaskService;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.util.ServiceLocator;
import com.heisenberg.impl.AbstractProcessEngine;
import com.heisenberg.impl.MessageImpl;
import com.heisenberg.impl.TriggerBuilderImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.plugin.ActivityTypes;
import com.heisenberg.impl.plugin.DataTypes;


/**
 * @author Walter White
 */
public class ClientProcessEngine extends AbstractProcessEngine implements ServiceLocator {
  
  public static final Logger log = LoggerFactory.getLogger(ClientProcessEngine.class);

  protected String baseUrl;
  
  public ClientProcessEngine(ClientProcessEngineConfiguration configuration) {
    super(configuration);
    this.baseUrl = configuration.getBaseUrl();
  }

  @Override
  public DeployResult deployProcessDefinition(ProcessDefinitionBuilder process) {
    log.debug("HTTP POST /deploy");
    log.debug("HTTP "+jsonService.objectToJsonStringPretty(process));
    return null;
  }

  @Override
  public ProcessInstance startProcessInstance(TriggerBuilderImpl processInstance) {
    log.debug("HTTP POST /start");
    log.debug("HTTP "+jsonService.objectToJsonStringPretty(processInstance));
    return null;
  }

  @Override
  public ProcessInstanceImpl sendActivityInstanceMessage(MessageImpl notifyActivityInstanceBuilder) {
    return null;
  }
  
  @Override
  public ServiceLocator getServiceLocator() {
    return this;
  }

  @Override
  public ScriptService getScriptService() {
    throw newUnexpectedInvocationException();
  }

  @Override
  public TaskService getTaskService() {
    throw newUnexpectedInvocationException();
  }

  @Override
  public ActivityTypes getActivityTypes() {
    throw newUnexpectedInvocationException();
  }

  protected RuntimeException newUnexpectedInvocationException() {
    return new RuntimeException("I didn't think this was ever going to be called.");
  }
}

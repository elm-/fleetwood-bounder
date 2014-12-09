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
package com.heisenberg.impl;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.builder.ActivityInstanceQuery;
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.builder.TriggerBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.util.ServiceLocator;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;


/**
 * @author Walter White
 */
public abstract class AbstractProcessEngine implements ProcessEngine {

  public abstract DeployResult deployProcessDefinition(ProcessDefinitionBuilder processBuilder);

  public abstract ProcessInstance startProcessInstance(TriggerBuilderImpl processInstanceBuilder);

  public abstract ProcessInstanceImpl sendActivityInstanceMessage(MessageImpl notifyActivityInstanceBuilder);

  public abstract ServiceLocator getServiceLocator();

  @Override
  public ActivityInstanceQuery newActivityInstanceQuery() {
    throw new RuntimeException("TODO");
  }

  @Override
  public ProcessDefinitionBuilder newProcessDefinition() {
    return new ProcessDefinitionImpl(this);
  }

  @Override
  public TriggerBuilder newTrigger() {
    return new TriggerBuilderImpl(this);
  }

  @Override
  public MessageBuilder newMessage() {
    return new MessageImpl(this);
  }
}

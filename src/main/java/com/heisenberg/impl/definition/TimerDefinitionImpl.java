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
package com.heisenberg.impl.definition;

import com.heisenberg.api.builder.TimerBuilder;
import com.heisenberg.impl.AbstractProcessEngine;
import com.heisenberg.impl.job.JobType;


/**
 * @author Walter White
 */
public class TimerDefinitionImpl implements TimerBuilder {

  public String name;
  public AbstractProcessEngine processEngine;
  public ProcessDefinitionImpl processDefinition;
  public ScopeDefinitionImpl parent;
  public JobType jobType;
  
  public void validate(ProcessDefinitionValidator validateProcessDefinitionAfterDeserialization) {
  }

  @Override
  public TimerBuilder name(String name) {
    return null;
  }

  @Override
  public TimerBuilder duedateAfterCreation(long millis) {
    return null;
  }

  @Override
  public TimerBuilder repeatAfterExecution(long millis) {
    return null;
  }

}

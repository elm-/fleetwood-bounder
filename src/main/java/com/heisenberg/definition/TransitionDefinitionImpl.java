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
package com.heisenberg.definition;

import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class TransitionDefinitionImpl {

  protected ActivityDefinitionImpl from;
  protected ActivityDefinitionImpl to;

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinitionImpl processDefinition;
  protected ScopeDefinitionImpl parent;

  public void prepare() {
  }

  public ActivityDefinitionImpl getFrom() {
    return from;
  }

  
  public void setFrom(ActivityDefinitionImpl from) {
    this.from = from;
  }

  
  public ActivityDefinitionImpl getTo() {
    return to;
  }

  
  public void setTo(ActivityDefinitionImpl to) {
    this.to = to;
  }

  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  
  public ScopeDefinitionImpl getParent() {
    return parent;
  }

  
  public void setParent(ScopeDefinitionImpl parent) {
    this.parent = parent;
  }

  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
}

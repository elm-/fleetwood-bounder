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

import com.heisenberg.instance.ProcessEngineImpl;
import com.heisenberg.util.Identifyable;


/**
 * @author Walter White
 */
public class TransitionDefinition implements Identifyable {

  protected TransitionDefinitionId id;
  protected ActivityDefinition from;
  protected ActivityDefinition to;

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinition processDefinition;
  protected ScopeDefinition parent;

  public void prepare() {
  }

  public TransitionDefinitionId getId() {
    return id;
  }
  
  public void setId(TransitionDefinitionId id) {
    this.id = id;
  }

  
  public ActivityDefinition getFrom() {
    return from;
  }

  
  public void setFrom(ActivityDefinition from) {
    this.from = from;
  }

  
  public ActivityDefinition getTo() {
    return to;
  }

  
  public void setTo(ActivityDefinition to) {
    this.to = to;
  }

  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  
  public ScopeDefinition getParent() {
    return parent;
  }

  
  public void setParent(ScopeDefinition parent) {
    this.parent = parent;
  }

  
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
}

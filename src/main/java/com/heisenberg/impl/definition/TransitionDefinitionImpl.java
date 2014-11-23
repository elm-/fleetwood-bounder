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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.builder.TransitionBuilder;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class TransitionDefinitionImpl implements TransitionBuilder, TransitionDefinition {

  public String name;
  @JsonIgnore
  public ActivityDefinitionImpl from;
  @JsonIgnore
  public ActivityDefinitionImpl to;

  @JsonIgnore
  public ProcessEngineImpl processEngine;
  @JsonIgnore
  public ProcessDefinitionImpl processDefinition;
  @JsonIgnore
  public ScopeDefinitionImpl parent;
  
  public String fromName;
  public String toName;
  public Long line;
  public Long column;

  public TransitionDefinitionImpl name(String name) {
    this.name = name;
    return this;
  }

  public TransitionDefinitionImpl line(Long line) {
    this.line = line;
    return this;
  }

  public TransitionDefinitionImpl column(Long column) {
    this.column = column;
    return this;
  }
  
  /** Fluent builder to set the source of this transition.
   * @param fromActivityDefinitionName the name of the activity definition. */
  public TransitionDefinitionImpl from(String fromName) {
    this.fromName = fromName;
    return this;
  }

  public TransitionDefinitionImpl to(String toName) {
    this.toName = toName;
    return this;
  }
  
  public void preSerialize() {
    if (from!=null) fromName = from.name;
    if (to!=null) toName = to.name;
  }
  
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

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
import com.heisenberg.api.configuration.Script;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.impl.AbstractProcessEngine;


/**
 * @author Walter White
 */
public class TransitionDefinitionImpl implements TransitionBuilder, TransitionDefinition {

  @JsonIgnore
  public ActivityDefinitionImpl from;
  @JsonIgnore
  public ActivityDefinitionImpl to;

  @JsonIgnore
  public AbstractProcessEngine processEngine;
  @JsonIgnore
  public ProcessDefinitionImpl processDefinition;
  @JsonIgnore
  public ScopeDefinitionImpl parent;
  
  public String id;
  public String fromId;
  public String toId;
  public Long line;
  public Long column;
  public String condition;
  
  @JsonIgnore
  public Script conditionScript;

  public TransitionDefinitionImpl id(String id) {
    this.id = id;
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
  public TransitionDefinitionImpl from(String fromId) {
    this.fromId = fromId;
    return this;
  }

  public TransitionDefinitionImpl to(String toId) {
    this.toId = toId;
    return this;
  }
  
  public TransitionDefinitionImpl condition(String condition) {
    this.condition = condition;
    return this;
  }
  
  public void preSerialize() {
    if (from!=null) fromId = from.id;
    if (to!=null) toId = to.id;
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

  
  public AbstractProcessEngine getProcessEngine() {
    return processEngine;
  }

  
  public void setProcessEngine(AbstractProcessEngine processEngine) {
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

  
  public Script getConditionScript() {
    return conditionScript;
  }

  
  public void setConditionScript(Script conditionScript) {
    this.conditionScript = conditionScript;
  }
  
  public String getId() {
    return id;
  }
}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fleetwood.bounder.instance.ProcessEngineImpl;
import fleetwood.bounder.util.Identifyable;


/**
 * @author Walter White
 */
public class TransitionDefinition implements Identifyable {

  protected TransitionDefinitionId id;
  protected ActivityDefinition from;
  protected ActivityDefinition to;

  @JsonIgnore
  protected ProcessEngineImpl processEngine;
  @JsonIgnore
  protected ProcessDefinition processDefinition;
  @JsonIgnore
  protected CompositeDefinition parent;

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

  
  public ProcessEngineImpl getProcessStore() {
    return processEngine;
  }

  
  public void setProcessStore(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  
  public CompositeDefinition getParent() {
    return parent;
  }

  
  public void setParent(CompositeDefinition parent) {
    this.parent = parent;
  }

  
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
}

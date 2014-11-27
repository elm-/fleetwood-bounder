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

import java.util.Map;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.builder.ProcessBuilder;
import com.heisenberg.api.definition.ProcessDefinition;


/**
 * @author Walter White
 */
public class ProcessDefinitionImpl extends ScopeDefinitionImpl implements ProcessBuilder, ProcessDefinition {

  /** The globally unique identifier for this process definition. */
  public Object id;
  
  /** optional time when the process was deployed.
   * This field just serves as a read/write property and is not used during process execution. */
  public LocalDateTime deployedTime;

  /** optional reference to the user that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public Object deployedBy;

  /** optional reference to the organization (aka tenant or workspace) that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public Object organizationId;

  /** optional reference to the the source process for which this process definition is one version.
   * This field just serves as a read/write property and is not used during process execution. */
  public Object processId;

  /** optional version number of this process definition, related to @link {@link #processId}.
   * This combined with the @link {@link ScopeDefinitionImpl#id} should be unique. */
  public Long version;
  
  @JsonIgnore
  public Map<Object, ActivityDefinitionImpl> activityDefinitionsMap;
  
  @JsonIgnore
  public Map<Object, VariableDefinitionImpl> variableDefinitionsMap;

  
  /// Process Definition Builder methods /////////////////////////////////////////////

  @Override
  public ProcessDefinitionImpl deployedTime(LocalDateTime deployedAt) {
    this.deployedTime = deployedAt;
    return this;
  }

  @Override
  public ProcessDefinitionImpl deployedUserId(Object deployedBy) {
    this.deployedBy = deployedBy;
    return this;
  }

  @Override
  public ProcessDefinitionImpl processId(Object processId) {
    this.processId = processId;
    return this;
  }

  @Override
  public ProcessDefinitionImpl version(Long version) {
    this.version = version;
    return this;
  }
  
  @Override
  public ProcessDefinitionImpl organizationId(Object organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  @Override
  public ProcessDefinitionImpl line(Long line) {
    super.line(line);
    return this;
  }

  @Override
  public ProcessDefinitionImpl column(Long column) {
    super.column(column);
    return this;
  }
  
  @Override
  public ActivityDefinitionImpl newActivity() {
    return super.newActivity();
  }

  @Override
  public VariableDefinitionImpl newVariable() {
    return super.newVariable();
  }

  @Override
  public TransitionDefinitionImpl newTransition() {
    return super.newTransition();
  }

  @Override
  public TimerDefinitionImpl newTimer() {
    return super.newTimer();
  }
  
  // other methods ////////////////////////////////////////////////////////////////////

  /** searches in this whole process definition.  activity ids must be unique over the whole process. */
  public ActivityDefinitionImpl findActivityDefinition(Object activityDefinitionId) {
    return activityDefinitionsMap.get(activityDefinitionId); 
  }
  
  /** searches in this whole process definition.  variable ids must be unique over the whole process. */
  public VariableDefinitionImpl findVariableDefinition(Object variableDefinitionId) {
    return variableDefinitionsMap.get(variableDefinitionId); 
  }
  
  public ProcessDefinitionPath getPath() {
    return new ProcessDefinitionPath();
  }

  public Object getId() {
    return id;
  }
  
  public void setId(Object id) {
    this.id = id;
  }
  
  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }
  
  // visitor methods ////////////////////////////////////////////////////////////////////
  
  public void visit(ProcessDefinitionVisitor visitor) {
    if (visitor==null) {
      return;
    }
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitor.startProcessDefinition(this);
    super.visit(visitor);
    visitor.endProcessDefinition(this);
  }
}

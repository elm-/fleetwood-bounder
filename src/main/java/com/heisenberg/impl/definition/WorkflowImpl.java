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
import com.heisenberg.api.builder.DeployResult;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.definition.Workflow;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.job.JobType;


/**
 * @author Walter White
 */
public class WorkflowImpl extends ScopeImpl implements WorkflowBuilder, Workflow {

  /** optional time when the process was deployed.
   * This field just serves as a read/write property and is not used during process execution. */
  public LocalDateTime deployedTime;

  /** optional reference to the user that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public String deployedBy;

  /** optional reference to the organization (aka tenant or workspace) that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public String organizationId;

  /** optional reference to the the source process for which this process definition is one version.
   * This field just serves as a read/write property and is not used during process execution. */
  public String processId;

  /** optional version number of this process definition, related to @link {@link #processId}.
   * This combined with the @link {@link ScopeImpl#id} should be unique. */
  public Long version;
  
  @JsonIgnore
  public Map<Object, ActivityImpl> activityMap;
  
  @JsonIgnore
  public Map<Object, VariableImpl> variableMap;


  public WorkflowImpl() {
  }

  public WorkflowImpl(WorkflowEngineImpl processEngine) {
    this.processEngine = processEngine;
    this.processDefinition = this;
  }


  /// Process Definition Builder methods /////////////////////////////////////////////

  public WorkflowImpl deployedTime(LocalDateTime deployedAt) {
    this.deployedTime = deployedAt;
    return this;
  }

  @Override
  public DeployResult deploy() {
    return processEngine.deployWorkflow(this);
  }

  @Override
  public WorkflowImpl name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public WorkflowImpl deployedUserId(String deployedBy) {
    this.deployedBy = deployedBy;
    return this;
  }

  @Override
  public WorkflowImpl processId(String processId) {
    this.processId = processId;
    return this;
  }

  @Override
  public WorkflowImpl version(Long version) {
    this.version = version;
    return this;
  }
  
  @Override
  public WorkflowImpl organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  @Override
  public WorkflowImpl line(Long line) {
    super.line(line);
    return this;
  }

  @Override
  public WorkflowImpl column(Long column) {
    super.column(column);
    return this;
  }
  
  @Override
  public ActivityImpl newActivity() {
    return super.newActivity();
  }

  @Override
  public VariableImpl newVariable() {
    return super.newVariable();
  }

  @Override
  public TransitionImpl newTransition() {
    return super.newTransition();
  }

  @Override
  public TimerDefinitionImpl newTimer(JobType jobType) {
    return super.newTimer(jobType);
  }
  
  // other methods ////////////////////////////////////////////////////////////////////

  /** searches in this whole process definition.  activity ids must be unique over the whole process. */
  public ActivityImpl findActivity(Object activityId) {
    return activityMap.get(activityId); 
  }
  
  /** searches in this whole process definition.  variable ids must be unique over the whole process. */
  public VariableImpl findVariable(Object variableId) {
    return variableMap.get(variableId); 
  }
  
  public WorkflowPath getPath() {
    return new WorkflowPath();
  }

  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }
  
  // visitor methods ////////////////////////////////////////////////////////////////////
  
  public void visit(WorkflowVisitor visitor) {
    if (visitor==null) {
      return;
    }
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitor.startWorkflow(this);
    super.visit(visitor);
    visitor.endWorkflow(this);
  }
}

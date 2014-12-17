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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.job.JobType;
import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public class ActivityDefinitionImpl extends ScopeDefinitionImpl implements ActivityBuilder, ActivityDefinition {

  public ActivityType activityType;

  /** the list of transitions for which this activity is the destination.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeDefinitionImpl#transitionDefinitions} */
  @JsonIgnore
  public List<TransitionDefinitionImpl> incomingTransitionDefinitions;

  /** the list of transitions for which this activity is the source.
   * This field is not persisted nor jsonned. It is derived from the parent's {@link ScopeDefinitionImpl#transitionDefinitions} */
  @JsonIgnore
  public List<TransitionDefinitionImpl> outgoingTransitionDefinitions;

  public String defaultTransitionId;
  
  @JsonIgnore
  public TransitionDefinitionImpl defaultTransition;
  
  public Binding<List<Object>> forEach;
  public VariableDefinitionImpl forEachElement;

  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public ActivityDefinitionImpl activityType(ActivityType activityType) {
    this.activityType = activityType;
    return this;
  }
  
  public ActivityDefinitionImpl defaultOutgoingTransitionId(String defaultOutgoingTransitionId) {
    this.defaultTransitionId = defaultOutgoingTransitionId;
    return this;
  }
  
  public ActivityDefinitionImpl id(String id) {
    this.id = id;
    return this;
  }

  public ActivityDefinitionImpl line(Long line) {
    super.line(line);
    return this;
  }

  public ActivityDefinitionImpl column(Long column) {
    super.column(column);
    return this;
  }
  
  @Override
  public ActivityBuilder defaultTransition(String transitionId) {
    this.defaultTransitionId = transitionId;
    return this;
  }
  
  @Override
  public ActivityBuilder forEach(String elementVariableId, DataType elementDataType, String collectionVariableId) {
    forEach(elementVariableId, elementDataType, new Binding<List<Object>>().variableDefinitionId(collectionVariableId));
    return this;
  }
  
  @Override
  public ActivityBuilder forEachExpression(String elementVariableId, DataType elementDataType, String collectionExpression) {
    forEach(elementVariableId, elementDataType, new Binding<List<Object>>().expression(collectionExpression));
    return this;
  }

  protected void forEach(String elementVariableId, DataType elementDataType, Binding<List<Object>> collectionBinding) {
    this.forEachElement = new VariableDefinitionImpl()
      .id(elementVariableId)
      .dataType(elementDataType)
      .processEngine(processEngine)
      .processDefinition(processDefinition)
      .parent(this);
    this.forEach = collectionBinding;
  }

  @Override
  public ActivityDefinitionImpl newActivity() {
    return super.newActivity();
  }
  
  @Override
  public ActivityDefinitionImpl newActivity(String id, ActivityType activityType) {
    return super.newActivity(id, activityType);
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
  public TimerDefinitionImpl newTimer(JobType jobType) {
    return super.newTimer(jobType);
  }
  
  /// other methods ////////////////////////////

  public ProcessDefinitionPath getPath() {
    return parent.getPath().addActivityDefinitionId(id);
  }

  public boolean isAsync(ActivityInstanceImpl activityInstance) {
    return false;
  }

  public void visit(ProcessDefinitionVisitor visitor, int index) {
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitor.startActivityDefinition(this, index);
    super.visit(visitor);
    visitor.endActivityDefinition(this, index);
  }

  public void addOutgoingTransition(TransitionDefinitionImpl transitionDefinition) {
    if (outgoingTransitionDefinitions==null) {
      outgoingTransitionDefinitions = new ArrayList<TransitionDefinitionImpl>();
    }
    outgoingTransitionDefinitions.add(transitionDefinition);
  }

  public boolean hasOutgoingTransitionDefinitions() {
    return outgoingTransitionDefinitions!=null && !outgoingTransitionDefinitions.isEmpty();
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<TransitionDefinition> getOutgoingTransitionDefinitions() {
    return (List) outgoingTransitionDefinitions;
  }

  public void setOutgoingTransitionDefinitions(List<TransitionDefinitionImpl> outgoingTransitionDefinitions) {
    this.outgoingTransitionDefinitions = outgoingTransitionDefinitions;
  }


  public void addIncomingTransition(TransitionDefinitionImpl transitionDefinition) {
    if (incomingTransitionDefinitions==null) {
      incomingTransitionDefinitions = new ArrayList<TransitionDefinitionImpl>();
    }
    incomingTransitionDefinitions.add(transitionDefinition);
  }

  public boolean hasIncomingTransitionDefinitions() {
    return incomingTransitionDefinitions!=null && !incomingTransitionDefinitions.isEmpty();
  }

  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<TransitionDefinition> getIncomingTransitionDefinitions() {
    return (List) incomingTransitionDefinitions;
  }

  public void setIncomingTransitionDefinitions(List<TransitionDefinitionImpl> incomingTransitionDefinitions) {
    this.incomingTransitionDefinitions = incomingTransitionDefinitions;
  }
  
  public ActivityType getActivityType() {
    return activityType;
  }
  
  public void setActivityType(ActivityType activityType) {
    this.activityType = activityType;
  }

  public String getDefaultTransitionId() {
    return defaultTransitionId;
  }

  public void setDefaultTransitionId(String defaultOutgoingTransitionId) {
    this.defaultTransitionId = defaultOutgoingTransitionId;
  }

  public TransitionDefinitionImpl getDefaultTransition() {
    return defaultTransition;
  }
  
  public void setDefaultTransition(TransitionDefinitionImpl defaultTransition) {
    this.defaultTransition = defaultTransition;
  }

  public String toString() {
    return id!=null ? "["+id.toString()+"]" : "["+Integer.toString(System.identityHashCode(this))+"]";
  }
}

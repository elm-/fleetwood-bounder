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
import com.heisenberg.api.definition.Activity;
import com.heisenberg.api.definition.Scope;
import com.heisenberg.api.definition.Transition;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.job.JobType;
import com.heisenberg.impl.plugin.ActivityType;
import com.heisenberg.impl.plugin.Validator;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public abstract class ScopeImpl implements Scope {

  // parsed and stored member fields
  
  public String id;

  public String name;

  public List<ActivityImpl> activityDefinitions;
  public List<VariableImpl> variableDefinitions;
  public List<TransitionImpl> transitionDefinitions;
  public List<TimerDefinitionImpl> timerDefinitions;

  // derived fields that are initialized in the prepare() method

  @JsonIgnore
  public WorkflowEngineImpl processEngine;
  @JsonIgnore
  public WorkflowImpl processDefinition;
  @JsonIgnore
  public ScopeImpl parent;
  @JsonIgnore
  public List<ActivityImpl> startActivities;

  public Long line;
  public Long column;
  
  /// Process Definition Builder methods //////////////////////////////////////////
  
  public ScopeImpl id(String id) {
    this.id = id;
    return this;
  }
  
  public ScopeImpl name(String name) {
    this.name = name;
    return this;
  }

  public ActivityImpl newActivity(String id, ActivityType activityType) {
    ActivityImpl activity = newActivity();
    activity.id(id);
    activity.activityType(activityType);
    return activity;
  }

  public ActivityImpl newActivity() {
    ActivityImpl activityDefinition = new ActivityImpl();
    activityDefinition.processEngine = this.processEngine;
    activityDefinition.processDefinition = this.processDefinition;
    activityDefinition.parent = this;
    if (activityDefinitions==null) {
      activityDefinitions = new ArrayList<>();
    }
    activityDefinitions.add(activityDefinition);
    return activityDefinition;
  }

  public VariableImpl newVariable() {
    VariableImpl variableDefinition = new VariableImpl();
    variableDefinition.workflowEngine = this.processEngine;
    variableDefinition.workflow = this.processDefinition;
    variableDefinition.parent = this;
    if (variableDefinitions==null) {
      variableDefinitions = new ArrayList<>();
    }
    variableDefinitions.add(variableDefinition);
    return variableDefinition;
  }

  public TransitionImpl newTransition() {
    TransitionImpl transitionDefinition = new TransitionImpl();
    transitionDefinition.processEngine = this.processEngine;
    transitionDefinition.processDefinition = this.processDefinition;
    transitionDefinition.parent = this;
    if (transitionDefinitions==null) {
      transitionDefinitions = new ArrayList<>();
    }
    transitionDefinitions.add(transitionDefinition);
    return transitionDefinition;
  }

  public TimerDefinitionImpl newTimer(JobType jobType) {
    TimerDefinitionImpl timerDefinition = new TimerDefinitionImpl();
    timerDefinition.processEngine = this.processEngine;
    timerDefinition.processDefinition = this.processDefinition;
    timerDefinition.parent = this;
    if (timerDefinitions==null) {
      timerDefinitions = new ArrayList<>();
    }
    timerDefinitions.add(timerDefinition);
    return timerDefinition;
  }

  public ScopeImpl line(Long line) {
    this.line = line;
    return this;
  }

  public ScopeImpl column(Long column) {
    this.column = column;
    return this;
  }

  /// Process Definition Parsing methods //////////////////////////////////////////

  public abstract WorkflowPath getPath();
  
  public ActivityImpl getActivity(String activityDefinitionId) {
    return processDefinition.findActivity(activityDefinitionId);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<Activity> getStartActivities() {
    return (List)startActivities;
  }
  
  public void setStartActivities(List<ActivityImpl> startActivityDefinitions) {
    this.startActivities = startActivityDefinitions;
  }

  public WorkflowImpl getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(WorkflowImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  public WorkflowEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(WorkflowEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public ScopeImpl getParent() {
    return parent;
  }

  public void setParent(ScopeImpl parent) {
    this.parent = parent;
  }
  
  public boolean isProcessDefinition() {
    return parent!=null;
  }

  public <T extends ActivityImpl> T addActivityDefinition(T activityDefinition) {
    Exceptions.checkNotNull(activityDefinition, "activityDefinition");
    if (activityDefinitions==null)  {
      activityDefinitions = new ArrayList<>();
    }
    activityDefinitions.add(activityDefinition);
    return activityDefinition;
  }
  
  public boolean hasActivityDefinitions() {
    return activityDefinitions!=null && !activityDefinitions.isEmpty();
  }

  public  ScopeImpl addVariableDefinition(VariableImpl variableDefinition) {
    Exceptions.checkNotNull(variableDefinition, "variableDefinition");
    if (variableDefinitions==null)  {
      variableDefinitions = new ArrayList<>();
    }
    variableDefinitions.add(variableDefinition);
    return this;
  }
  
  public boolean hasVariableDefinitions() {
    return variableDefinitions!=null && !variableDefinitions.isEmpty();
  }

  public ScopeImpl addTransitionDefinition(TransitionImpl transitionDefinition) {
    Exceptions.checkNotNull(transitionDefinition, "transitionDefinition");
    if (transitionDefinitions==null)  {
      transitionDefinitions = new ArrayList<>();
    }
    transitionDefinitions.add(transitionDefinition);
    return this;
  }
  
  /// visistor ////////////////////////////////////////////////////////////

  public void visit(WorkflowVisitor visitor) {
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitCompositeActivityDefinitions(visitor);
    visitCompositeTransitionDefinitions(visitor);
    visitCompositeVariableDefinitions(visitor);
  }

  protected void visitCompositeActivityDefinitions(WorkflowVisitor visitor) {
    if (activityDefinitions!=null) {
      for (int i=0; i<activityDefinitions.size(); i++) {
        ActivityImpl activityDefinition = activityDefinitions.get(i);
        activityDefinition.visit(visitor, i);
      }
    }
  }

  protected void visitCompositeVariableDefinitions(WorkflowVisitor visitor) {
    if (variableDefinitions!=null) {
      for (int i=0; i<variableDefinitions.size(); i++) {
        VariableImpl variableDefinition = variableDefinitions.get(i);
        visitor.variableDefinition(variableDefinition, i);
      }
    }
  }

  protected void visitCompositeTransitionDefinitions(WorkflowVisitor visitor) {
    if (transitionDefinitions!=null) {
      for (int i=0; i<transitionDefinitions.size(); i++) {
        TransitionImpl transitionDefinition = transitionDefinitions.get(i);
        visitor.transitionDefinition(transitionDefinition, i);
      }
    }
  }

  public boolean containsVariable(Object variableDefinitionId) {
    if (variableDefinitionId==null) {
      return false;
    }
    if (variableDefinitions!=null) {
      for (VariableImpl variableDefinition: variableDefinitions) {
        if (variableDefinitionId.equals(variableDefinition.id)) {
          return true;
        }
      }
    }
    ScopeImpl parent = getParent();
    if (parent!=null) {
      return parent.containsVariable(variableDefinitionId);
    }
    return false;
  }
  
  public void initializeStartActivities(Validator validator) {
    if (activityDefinitions!=null && !activityDefinitions.isEmpty()) {
      this.startActivities = new ArrayList<>(activityDefinitions);
      if (transitionDefinitions!=null) {
        for (TransitionImpl transition: transitionDefinitions) {
          this.startActivities.remove(transition.getTo());
        }
      }
    }
    if (startActivities==null) {
      validator.addWarning("No start activities in %s", getId());
    }
  }
  
  // getters and setters ////////////////////////////////////////////////////////////
  
  public boolean hasTransitionDefinitions() {
    return transitionDefinitions!=null && !transitionDefinitions.isEmpty();
  } 
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Activity> getActivityDefinitions() {
    return (List<Activity>) (List) activityDefinitions;
  }
  
  public void setActivityDefinitions(List<ActivityImpl> activityDefinitions) {
    this.activityDefinitions = activityDefinitions;
  }

  public List<VariableImpl> getVariableDefinitions() {
    return variableDefinitions;
  }

  public void setVariableDefinitions(List<VariableImpl> variableDefinitions) {
    this.variableDefinitions = variableDefinitions;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<Transition> getTransitions() {
    return (List) transitionDefinitions;
  }
  
  public void setTransitionDefinitions(List<TransitionImpl> transitionDefinitions) {
    this.transitionDefinitions = transitionDefinitions;
  }

  public List<TimerDefinitionImpl> getTimerDefinitions() {
    return timerDefinitions;
  }
  
  public void setTimerDefinitions(List<TimerDefinitionImpl> timerDefinitions) {
    this.timerDefinitions = timerDefinitions;
  }
  
  public Long getLine() {
    return line;
  }
  
  public void setLine(Long line) {
    this.line = line;
  }
  
  public Long getColumn() {
    return column;
  }
  
  public void setColumn(Long column) {
    this.column = column;
  }
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.util.ActivityDefinitionId;
import com.heisenberg.api.util.Id;
import com.heisenberg.api.util.Validator;
import com.heisenberg.api.util.VariableDefinitionId;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public abstract class ScopeDefinitionImpl {

  // parsed and stored member fields

  public List<ActivityDefinitionImpl> activityDefinitions;
  public List<VariableDefinitionImpl> variableDefinitions;
  public List<TransitionDefinitionImpl> transitionDefinitions;
  public List<TimerDefinitionImpl> timerDefinitions;

  // derived fields that are initialized in the prepare() method

  @JsonIgnore
  public ProcessEngineImpl processEngine;
  @JsonIgnore
  public ProcessDefinitionImpl processDefinition;
  @JsonIgnore
  public ScopeDefinitionImpl parent;
  @JsonIgnore
  public Map<ActivityDefinitionId, ActivityDefinitionImpl> activityDefinitionsMap;
  @JsonIgnore
  public Map<VariableDefinitionId, VariableDefinitionImpl> variableDefinitionsMap;
  @JsonIgnore
  public List<ActivityDefinitionImpl> startActivities;

  public Long line;
  public Long column;
  
  /// Process Definition Builder methods //////////////////////////////////////////
  
  public ActivityDefinitionImpl newActivity() {
    ActivityDefinitionImpl activityDefinition = new ActivityDefinitionImpl();
    activityDefinition.processEngine = this.processEngine;
    activityDefinition.processDefinition = this.processDefinition;
    activityDefinition.parent = this;
    if (activityDefinitions==null) {
      activityDefinitions = new ArrayList<>();
    }
    activityDefinitions.add(activityDefinition);
    return activityDefinition;
  }

  public VariableDefinitionImpl newVariable() {
    VariableDefinitionImpl variableDefinition = new VariableDefinitionImpl();
    variableDefinition.processEngine = this.processEngine;
    variableDefinition.processDefinition = this.processDefinition;
    variableDefinition.parent = this;
    if (variableDefinitions==null) {
      variableDefinitions = new ArrayList<>();
    }
    variableDefinitions.add(variableDefinition);
    return variableDefinition;
  }

  public TransitionDefinitionImpl newTransition() {
    TransitionDefinitionImpl transitionDefinition = new TransitionDefinitionImpl();
    transitionDefinition.processEngine = this.processEngine;
    transitionDefinition.processDefinition = this.processDefinition;
    transitionDefinition.parent = this;
    if (transitionDefinitions==null) {
      transitionDefinitions = new ArrayList<>();
    }
    transitionDefinitions.add(transitionDefinition);
    return transitionDefinition;
  }

  public TimerDefinitionImpl newTimer() {
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

  public ScopeDefinitionImpl line(Long line) {
    this.line = line;
    return this;
  }

  public ScopeDefinitionImpl column(Long column) {
    this.column = column;
    return this;
  }

  /// Process Definition Parsing methods //////////////////////////////////////////

  public abstract ProcessDefinitionPath getPath();

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List<ActivityDefinition> getStartActivities() {
    return (List)startActivities;
  }
  
  public void setStartActivities(List<ActivityDefinitionImpl> startActivityDefinitions) {
    this.startActivities = startActivityDefinitions;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  public ActivityDefinitionImpl getActivityDefinition(Object idInternal) {
    return activityDefinitionsMap!=null ? activityDefinitionsMap.get(new ActivityDefinitionId(idInternal)) : null;
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
  
  public boolean isProcessDefinition() {
    return parent!=null;
  }

  public <T extends ActivityDefinitionImpl> T addActivityDefinition(T activityDefinition) {
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

  public VariableDefinitionImpl getVariableDefinition(String name) {
    return variableDefinitionsMap!=null ? variableDefinitionsMap.get(name) : null;
  }
  
  public  ScopeDefinitionImpl addVariableDefinition(VariableDefinitionImpl variableDefinition) {
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

  public void createTransitionDefinition(ActivityDefinitionImpl from, ActivityDefinitionImpl to) {
    TransitionDefinitionImpl transitionDefinition = new TransitionDefinitionImpl();
    transitionDefinition.setFrom(from);
    transitionDefinition.setTo(to);
    addTransitionDefinition(transitionDefinition);
    from.addOutgoingTransition(transitionDefinition);
  }

  public ScopeDefinitionImpl addTransitionDefinition(TransitionDefinitionImpl transitionDefinition) {
    Exceptions.checkNotNull(transitionDefinition, "transitionDefinition");
    if (transitionDefinitions==null)  {
      transitionDefinitions = new ArrayList<>();
    }
    transitionDefinitions.add(transitionDefinition);
    return this;
  }
  
  /// visistor ////////////////////////////////////////////////////////////

  public void visit(ProcessDefinitionVisitor visitor) {
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitCompositeActivityDefinitions(visitor);
    visitCompositeTransitionDefinitions(visitor);
    visitCompositeVariableDefinitions(visitor);
  }

  protected void visitCompositeActivityDefinitions(ProcessDefinitionVisitor visitor) {
    if (activityDefinitions!=null) {
      for (int i=0; i<activityDefinitions.size(); i++) {
        ActivityDefinitionImpl activityDefinition = activityDefinitions.get(i);
        activityDefinition.visit(visitor, i);
      }
    }
  }

  protected void visitCompositeVariableDefinitions(ProcessDefinitionVisitor visitor) {
    if (variableDefinitions!=null) {
      for (int i=0; i<variableDefinitions.size(); i++) {
        VariableDefinitionImpl variableDefinition = variableDefinitions.get(i);
        visitor.variableDefinition(variableDefinition, i);
      }
    }
  }

  protected void visitCompositeTransitionDefinitions(ProcessDefinitionVisitor visitor) {
    if (transitionDefinitions!=null) {
      for (int i=0; i<transitionDefinitions.size(); i++) {
        TransitionDefinitionImpl transitionDefinition = transitionDefinitions.get(i);
        visitor.transitionDefinition(transitionDefinition, i);
      }
    }
  }

  public boolean containsVariable(VariableDefinitionId variableDefinitionId) {
    if (variableDefinitionId==null) {
      return false;
    }
    if (variableDefinitions!=null) {
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        if (variableDefinitionId.equals(variableDefinition.id)) {
          return true;
        }
      }
    }
    ScopeDefinitionImpl parent = getParent();
    if (parent!=null) {
      return parent.containsVariable(variableDefinitionId);
    }
    return false;
  }

  public VariableDefinitionImpl findVariableDefinitionById(VariableDefinitionId variableDefinitionId) {
    return variableDefinitionsMap!=null ? variableDefinitionsMap.get(variableDefinitionId) : null;
  }

  public void initializeActivityDefinitionsMap() {
    if (activityDefinitionsMap==null && activityDefinitions!=null) {
      activityDefinitionsMap = new HashMap<>();
      for (ActivityDefinitionImpl activityDefinition: activityDefinitions) {
        activityDefinitionsMap.put(activityDefinition.id, activityDefinition);
      }
    }
  }
  
  public void initializeStartActivities(Validator validator) {
    if (activityDefinitions!=null && !activityDefinitions.isEmpty()) {
      this.startActivities = new ArrayList<>(activityDefinitions);
      if (transitionDefinitions!=null) {
        for (TransitionDefinitionImpl transition: transitionDefinitions) {
          this.startActivities.remove(transition.getTo());
        }
      }
    }
    if (startActivities==null) {
      validator.addError("No start activities in %s", getId());
    }
  }
  
  public abstract Id getId();

  // getters and setters ////////////////////////////////////////////////////////////
  
  public boolean hasTransitionDefinitions() {
    return transitionDefinitions!=null && !transitionDefinitions.isEmpty();
  } 
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<ActivityDefinition> getActivityDefinitions() {
    return (List<ActivityDefinition>) (List) activityDefinitions;
  }
  
  public void setActivityDefinitions(List<ActivityDefinitionImpl> activityDefinitions) {
    this.activityDefinitions = activityDefinitions;
  }

  
  public Map<ActivityDefinitionId, ActivityDefinitionImpl> getActivityDefinitionsMap() {
    return activityDefinitionsMap;
  }

  
  public void setActivityDefinitionsMap(Map<ActivityDefinitionId, ActivityDefinitionImpl> activityDefinitionsMap) {
    this.activityDefinitionsMap = activityDefinitionsMap;
  }

  
  public List<VariableDefinitionImpl> getVariableDefinitions() {
    return variableDefinitions;
  }

  
  public void setVariableDefinitions(List<VariableDefinitionImpl> variableDefinitions) {
    this.variableDefinitions = variableDefinitions;
  }

  
  public Map<VariableDefinitionId, VariableDefinitionImpl> getVariableDefinitionsMap() {
    return variableDefinitionsMap;
  }

  
  public void setVariableDefinitionsMap(Map<VariableDefinitionId, VariableDefinitionImpl> variableDefinitionsMap) {
    this.variableDefinitionsMap = variableDefinitionsMap;
  }

  
  public List<TransitionDefinitionImpl> getTransitionDefinitions() {
    return transitionDefinitions;
  }

  
  public void setTransitionDefinitions(List<TransitionDefinitionImpl> transitionDefinitions) {
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
}

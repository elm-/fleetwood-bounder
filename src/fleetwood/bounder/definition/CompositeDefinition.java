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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fleetwood.bounder.store.ProcessStore;
import fleetwood.bounder.util.Exceptions;


/**
 * @author Tom Baeyens
 */
public class CompositeDefinition {

  protected ProcessStore processStore;
  protected ProcessDefinition processDefinition;
  protected Map<ActivityDefinitionId, ActivityDefinition> activityDefinitions;
  protected CompositeDefinition parent;
  protected Map<VariableDefinitionId, VariableDefinition> variableDefinitions;
  protected Map<TransitionDefinitionId, TransitionDefinition> transitionDefinitions;
  protected List<ActivityDefinition> startActivityDefinitions;
  
  /** performs initializations after the activity is constructed and before the process is used in execution.
   * eg calculating the start activities */ 
  public void prepare() {
    startActivityDefinitions = null;
    if (activityDefinitions!=null) {
      startActivityDefinitions = new ArrayList<>(activityDefinitions.values());
    }
    if (transitionDefinitions!=null) {
      for (TransitionDefinition transition: transitionDefinitions.values()) {
        startActivityDefinitions.remove(transition.getTo());
      }
      if (startActivityDefinitions.isEmpty()) {
        startActivityDefinitions = null;
      }
    }
    if (activityDefinitions!=null) {
      for (ActivityDefinition activityDefinition: activityDefinitions.values()) {
        activityDefinition.prepare();
      }
    }
  } 
  
  public List<ActivityDefinition> getStartActivityDefinitions() {
    return startActivityDefinitions;
  }
  
  public void setStartActivityDefinitions(List<ActivityDefinition> startActivityDefinitions) {
    this.startActivityDefinitions = startActivityDefinitions;
  }

  public ProcessStore getProcessStore() {
    return processStore;
  }

  public void setProcessStore(ProcessStore processStore) {
    this.processStore = processStore;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  public Map<ActivityDefinitionId, ActivityDefinition> getActivityDefinitions() {
    return activityDefinitions;
  }
  
  public ActivityDefinition getActivityDefinition(ActivityDefinitionId id) {
    return activityDefinitions!=null ? activityDefinitions.get(id) : null;
  }
  
  public void setActivityDefinitions(Map<ActivityDefinitionId, ActivityDefinition> activityDefinitions) {
    this.activityDefinitions = activityDefinitions;
  }
  
  public CompositeDefinition getParent() {
    return parent;
  }

  public void setParent(CompositeDefinition parent) {
    this.parent = parent;
  }

  public CompositeDefinition addActivityDefinition(ActivityDefinition activityDefinition) {
    Exceptions.checkNotNull(activityDefinition, "activityDefinition");
    activityDefinition.processStore = processStore;
    activityDefinition.processDefinition = processDefinition;
    activityDefinition.parent = this;
    if (activityDefinitions==null)  {
      activityDefinitions = new LinkedHashMap<>();
    }
    if (activityDefinition.id==null) {
      activityDefinition.id = processStore.createActivityDefinitionId(activityDefinition);
    }
    activityDefinitions.put(activityDefinition.id, activityDefinition);
    return this;
  }
  
  public Map<VariableDefinitionId, VariableDefinition> getVariableDefinitions() {
    return variableDefinitions;
  }
  
  public VariableDefinition getVariableDefinition(VariableDefinitionId id) {
    return variableDefinitions!=null ? variableDefinitions.get(id) : null;
  }
  
  public CompositeDefinition setVariableDefinitions(Map<VariableDefinitionId, VariableDefinition> variables) {
    this.variableDefinitions = variables;
    return this;
  }

  public CompositeDefinition addVariableDefinition(VariableDefinition variableDefinition) {
    Exceptions.checkNotNull(variableDefinition, "variableDefinition");
    variableDefinition.processStore = processStore;
    variableDefinition.parent = this;
    if (variableDefinitions==null)  {
      variableDefinitions = new LinkedHashMap<>();
    }
    if (variableDefinition.id==null) {
      variableDefinition.id = processStore.createVariableDefinitionId(variableDefinition);
    }
    variableDefinitions.put(variableDefinition.id, variableDefinition);
    return this;
  }
  
  public CompositeDefinition addTransitionDefinition(TransitionDefinition transition) {
    Exceptions.checkNotNull(transition, "transition");
    Exceptions.checkNotNull(transition.id, "transition.id");
    if (transitionDefinitions==null)  {
      transitionDefinitions = new LinkedHashMap<>();
    }
    transitionDefinitions.put(transition.id, transition);
    return this;
  }
  
  public Map<TransitionDefinitionId, TransitionDefinition> getTransitionDefinitions() {
    return transitionDefinitions;
  }
  
  public boolean hasTransitionDefinitions() {
    return transitionDefinitions!=null && !transitionDefinitions.isEmpty();
  } 
  
  public CompositeDefinition setTransitionDefinitions(Map<TransitionDefinitionId, TransitionDefinition> transitions) {
    this.transitionDefinitions = transitions;
    return this;
  }
  
  public TransitionDefinition getTransitionDefinition(TransitionDefinitionId id) {
    return transitionDefinitions!=null ? transitionDefinitions.get(id) : null;
  }
}

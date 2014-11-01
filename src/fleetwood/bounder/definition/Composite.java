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

import java.util.LinkedHashMap;
import java.util.Map;

import fleetwood.bounder.store.ProcessStore;
import fleetwood.bounder.util.Exceptions;


/**
 * @author Tom Baeyens
 */
public class Composite {

  protected ProcessStore processStore;
  protected ProcessDefinition processDefinition;
  protected Map<ActivityDefinitionId, ActivityDefinition> activityDefinitions;
  protected Map<VariableId, VariableDefinition> variables;
  protected Map<TransitionId, Transition> transitions;
  
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

  public Composite addActivityDefinition(ActivityDefinition activityDefinition) {
    Exceptions.checkNotNull(activityDefinition, "activityDefinition");
    if (activityDefinition.id==null) {
      activityDefinition.id = processStore.createActivityDefinitionId(processDefinition, activityDefinition);
    }
    if (activityDefinitions==null)  {
      activityDefinitions = new LinkedHashMap<>();
    }
    activityDefinitions.put(activityDefinition.id, activityDefinition);
    return this;
  }
  
  public Map<VariableId, VariableDefinition> getVariables() {
    return variables;
  }
  
  public VariableDefinition getVariable(VariableId id) {
    return variables!=null ? variables.get(id) : null;
  }
  
  public Composite setVariables(Map<VariableId, VariableDefinition> variables) {
    this.variables = variables;
    return this;
  }

  public Composite addVariable(VariableDefinition variableDefinition) {
    Exceptions.checkNotNull(variableDefinition, "variableDefinition");
    Exceptions.checkNotNull(variableDefinition.id, "variableDefinition.id");
    if (variableDefinition.id==null) {
      variableDefinition.id = processStore.createVariableDefinitionId(processDefinition, variableDefinition);
    }
    if (variables==null)  {
      variables = new LinkedHashMap<>();
    }
    variables.put(variableDefinition.id, variableDefinition);
    return this;
  }
  
  public Composite addTransition(Transition transition) {
    Exceptions.checkNotNull(transition, "transition");
    Exceptions.checkNotNull(transition.id, "transition.id");
    if (transitions==null)  {
      transitions = new LinkedHashMap<>();
    }
    transitions.put(transition.id, transition);
    return this;
  }
  
  public Map<TransitionId, Transition> getTransitions() {
    return transitions;
  }
  
  public Composite setTransitions(Map<TransitionId, Transition> transitions) {
    this.transitions = transitions;
    return this;
  }
  
  public Transition getTransition(TransitionId id) {
    return transitions!=null ? transitions.get(id) : null;
  }
}

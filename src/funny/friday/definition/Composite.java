/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.definition;

import java.util.LinkedHashMap;
import java.util.Map;

import funny.friday.store.ProcessStore;
import funny.friday.util.Exceptions;


/**
 * @author Tom Baeyens
 */
public class Composite {

  protected ProcessStore processStore;
  protected ProcessDefinition processDefinition;
  protected Map<ActivityDefinitionId, ActivityDefinition> activityDefinitions;
  protected Map<VariableId, Variable> variables;
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
      activityDefinitions = new LinkedHashMap();
    }
    activityDefinitions.put(activityDefinition.id, activityDefinition);
    return this;
  }
  
  public Map<VariableId, Variable> getVariables() {
    return variables;
  }
  
  public Variable getVariable(VariableId id) {
    return variables!=null ? variables.get(id) : null;
  }
  
  public Composite setVariables(Map<VariableId, Variable> variables) {
    this.variables = variables;
    return this;
  }

  public Composite addVariable(Variable variable) {
    Exceptions.checkNotNull(variable, "variable");
    Exceptions.checkNotNull(variable.id, "variable.id");
    if (variables==null)  {
      variables = new LinkedHashMap<>();
    }
    variables.put(variable.id, variable);
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

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
package com.heisenberg.api.definition;

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.spi.ActivityParameter;


/**
 * @author Walter White
 */
public class ScopeDefinition {

  public String name;
  public Location location;
  public List<ActivityDefinition> activityDefinitions;
  public List<VariableDefinition> variableDefinitions;
  public List<TransitionDefinition> transitionDefinitions;
  public List<TimerDefinition> timerDefinitions;
  
  public ScopeDefinition name(String name) {
    this.name = name;
    return this;
  }
  
  public ScopeDefinition activity(ActivityDefinition activityDefinition) {
    if (activityDefinitions==null) {
      activityDefinitions = new ArrayList<ActivityDefinition>();
    }
    if (activityDefinition.location==null) {
      activityDefinition.location = new Location();
    } 
    if (activityDefinition.location.path==null) {
      activityDefinition.location.path = addPathElement(activityDefinition.name, "activityDefinitions", activityDefinitions.size());
    }
    activityDefinitions.add(activityDefinition);
    return this;
  }

  public ScopeDefinition transition(ActivityDefinition fromActivityDefinition, ActivityDefinition toActivityDefinition) {
    return transition(fromActivityDefinition.name, toActivityDefinition.name);
  }

  public ScopeDefinition transition(String fromActivityDefinitionName, String toActivityDefinitionName) {
    transition(new TransitionDefinition()
      .from(fromActivityDefinitionName)
      .to(toActivityDefinitionName)
    );
    return this;
  }

  public ScopeDefinition transition(TransitionDefinition transitionDefinition) {
    if (transitionDefinitions==null) {
      transitionDefinitions = new ArrayList<TransitionDefinition>();
    }
    if (transitionDefinition.location==null) {
      transitionDefinition.location = new Location();
    } 
    if (transitionDefinition.location.path==null) {
      transitionDefinition.location.path = addPathElement(null, "transitionDefinitions", transitionDefinitions.size());
    }
    transitionDefinitions.add(transitionDefinition);
    return this;
  }

  public ScopeDefinition variable(VariableDefinition variableDefinition) {
    if (variableDefinitions==null) {
      variableDefinitions = new ArrayList<VariableDefinition>();
    }
    if (variableDefinition.location==null) {
      variableDefinition.location = new Location();
    } 
    if (variableDefinition.location.path==null) {
      variableDefinition.location.path = addPathElement(variableDefinition.name, "variableDefinitions", variableDefinitions.size());
    }
    variableDefinitions.add(variableDefinition);
    return this;
  }
  
  public ScopeDefinition timer(TimerDefinition timerDefinition) {
    if (timerDefinitions==null) {
      timerDefinitions = new ArrayList<TimerDefinition>();
    }
    timerDefinitions.add(timerDefinition);
    return this;
  }
  
  String addPathElement(String indexName, String collectionName, int index) {
    return (location!=null ? location.path+"." : "")+collectionName+"["+(indexName!=null ? indexName : index)+"]";
  }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fleetwood.bounder.engine.ProcessEngineImpl;
import fleetwood.bounder.util.Exceptions;
import fleetwood.bounder.util.Identifyable;


/**
 * @author Walter White
 */
public abstract class CompositeDefinition implements Identifyable {

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinition processDefinition;
  protected CompositeDefinition parent;
  protected List<ActivityDefinition> activityDefinitions;
  protected Map<ActivityDefinitionId, ActivityDefinition> activityDefinitionsMap;
  protected List<VariableDefinition> variableDefinitions;
  protected Map<VariableDefinitionId, VariableDefinition> variableDefinitionsMap;
  protected List<TransitionDefinition> transitionDefinitions;
  protected Map<TransitionDefinitionId, TransitionDefinition> transitionDefinitionsMap;

  @JsonIgnore
  protected List<ActivityDefinition> startActivityDefinitions;
  
  /** performs initializations after the activity is constructed and before the process is used in execution.
   * eg calculating the start activities */ 
  public void prepare() {
    if (activityDefinitions!=null) {
      startActivityDefinitions = new ArrayList<>(activityDefinitions);
      activityDefinitionsMap = new HashMap<>();
      for (ActivityDefinition activityDefinition: activityDefinitions) {
        activityDefinition.setProcessStore(processEngine);
        activityDefinition.setProcessDefinition(processDefinition);
        activityDefinition.setParent(this);
        Exceptions.checkNotNull(activityDefinition.getId(), "activityDefinition.id");
        activityDefinitionsMap.put(activityDefinition.getId(), activityDefinition);
        activityDefinition.prepare();
      }
    }
    if (transitionDefinitions!=null) {
      transitionDefinitionsMap = new HashMap<>();
      for (TransitionDefinition transitionDefinition: transitionDefinitions) {
        if (startActivityDefinitions!=null) {
          startActivityDefinitions.remove(transitionDefinition.getTo());
        }
        transitionDefinition.setProcessStore(processEngine);
        transitionDefinition.setProcessDefinition(processDefinition);
        transitionDefinition.setParent(this);
        Exceptions.checkNotNull(transitionDefinition.getId(), "transitionDefinition.id");
        transitionDefinitionsMap.put(transitionDefinition.getId(), transitionDefinition);
        transitionDefinition.prepare();
      }
    }
    if (variableDefinitions!=null) {
      variableDefinitionsMap = new HashMap<>();
      for (VariableDefinition variableDefinition: variableDefinitions) {
        variableDefinition.setProcessStore(processEngine);
        variableDefinition.setProcessDefinition(processDefinition);
        variableDefinition.setParent(this);
        Exceptions.checkNotNull(variableDefinition.getId(), "variableDefinition.id");
        variableDefinitionsMap.put(variableDefinition.getId(), variableDefinition);
        variableDefinition.prepare();
      }
    }
  } 
  
  public List<ActivityDefinition> getStartActivityDefinitions() {
    return startActivityDefinitions;
  }
  
  public void setStartActivityDefinitions(List<ActivityDefinition> startActivityDefinitions) {
    this.startActivityDefinitions = startActivityDefinitions;
  }

  public ProcessEngineImpl getProcessStore() {
    return processEngine;
  }

  public void setProcessStore(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  public ActivityDefinition getActivityDefinition(ActivityDefinitionId id) {
    return activityDefinitionsMap!=null ? activityDefinitionsMap.get(id) : null;
  }
  
  public CompositeDefinition getParent() {
    return parent;
  }

  public void setParent(CompositeDefinition parent) {
    this.parent = parent;
  }
  
  public boolean isProcessDefinition() {
    return parent!=null;
  }

  public CompositeDefinition addActivityDefinition(ActivityDefinition activityDefinition) {
    Exceptions.checkNotNull(activityDefinition, "activityDefinition");
    if (activityDefinitions==null)  {
      activityDefinitions = new ArrayList<>();
    }
    activityDefinitions.add(activityDefinition);
    return this;
  }
  
  public boolean hasActivityDefinitions() {
    return activityDefinitions!=null && !activityDefinitions.isEmpty();
  }

  public VariableDefinition getVariableDefinition(VariableDefinitionId id) {
    return variableDefinitionsMap!=null ? variableDefinitionsMap.get(id) : null;
  }
  
  public CompositeDefinition addVariableDefinition(VariableDefinition variableDefinition) {
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

  public void addTransitionDefinition(ActivityDefinition from, ActivityDefinition to) {
    TransitionDefinition transitionDefinition = new TransitionDefinition();
    transitionDefinition.setFrom(from);
    transitionDefinition.setTo(to);
    addTransitionDefinition(transitionDefinition);
  }

  public CompositeDefinition addTransitionDefinition(TransitionDefinition transitionDefinition) {
    Exceptions.checkNotNull(transitionDefinition, "transitionDefinition");
    if (transitionDefinitions==null)  {
      transitionDefinitions = new ArrayList<>();
    }
    transitionDefinitions.add(transitionDefinition);
    return this;
  }
  
  public boolean hasTransitionDefinitions() {
    return transitionDefinitions!=null && !transitionDefinitions.isEmpty();
  } 
  
  public TransitionDefinition getTransitionDefinition(TransitionDefinitionId id) {
    return transitionDefinitionsMap!=null ? transitionDefinitionsMap.get(id) : null;
  }

  
  public List<ActivityDefinition> getActivityDefinitions() {
    return activityDefinitions;
  }

  
  public void setActivityDefinitions(List<ActivityDefinition> activityDefinitions) {
    this.activityDefinitions = activityDefinitions;
  }

  
  public Map<ActivityDefinitionId, ActivityDefinition> getActivityDefinitionsMap() {
    return activityDefinitionsMap;
  }

  
  public void setActivityDefinitionsMap(Map<ActivityDefinitionId, ActivityDefinition> activityDefinitionsMap) {
    this.activityDefinitionsMap = activityDefinitionsMap;
  }

  
  public List<VariableDefinition> getVariableDefinitions() {
    return variableDefinitions;
  }

  
  public void setVariableDefinitions(List<VariableDefinition> variableDefinitions) {
    this.variableDefinitions = variableDefinitions;
  }

  
  public Map<VariableDefinitionId, VariableDefinition> getVariableDefinitionsMap() {
    return variableDefinitionsMap;
  }

  
  public void setVariableDefinitionsMap(Map<VariableDefinitionId, VariableDefinition> variableDefinitionsMap) {
    this.variableDefinitionsMap = variableDefinitionsMap;
  }

  
  public List<TransitionDefinition> getTransitionDefinitions() {
    return transitionDefinitions;
  }

  
  public void setTransitionDefinitions(List<TransitionDefinition> transitionDefinitions) {
    this.transitionDefinitions = transitionDefinitions;
  }

  
  public Map<TransitionDefinitionId, TransitionDefinition> getTransitionDefinitionsMap() {
    return transitionDefinitionsMap;
  }

  
  public void setTransitionDefinitionsMap(Map<TransitionDefinitionId, TransitionDefinition> transitionDefinitionsMap) {
    this.transitionDefinitionsMap = transitionDefinitionsMap;
  }
}

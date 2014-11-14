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
package com.heisenberg.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.api.definition.ParameterInstance;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.ScopeInstanceImpl;
import com.heisenberg.util.Exceptions;
import com.heisenberg.util.Identifyable;


/**
 * @author Walter White
 */
public abstract class ScopeDefinitionImpl implements Identifyable {

  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;
  public ScopeDefinitionImpl parent;
  public List<ActivityDefinitionImpl> startActivityDefinitions;
  public List<ActivityDefinitionImpl> activityDefinitions;
  public Map<ActivityDefinitionId, ActivityDefinitionImpl> activityDefinitionsMap;
  public List<VariableDefinitionImpl> variableDefinitions;
  public Map<VariableDefinitionId, VariableDefinitionImpl> variableDefinitionsMap;
  public List<TransitionDefinitionImpl> transitionDefinitions;
  public Map<TransitionDefinitionId, TransitionDefinitionImpl> transitionDefinitionsMap;
  public List<ParameterInstanceImpl> parameterInstances;
  public Map<String, ParameterInstanceImpl> parameterInstancesMap;
  public List<TimerDefinitionImpl> timerDefinitions;
  
  public ParameterDefinitionsImpl getParameterDefinitions() {
    return null;
  }

  /** performs initializations after the activity is constructed and before the process is used in execution.
   * eg calculating the start activities */ 
  public void prepare() {
    if (activityDefinitions!=null) {
      startActivityDefinitions = new ArrayList<>(activityDefinitions);
      activityDefinitionsMap = new HashMap<>();
      for (ActivityDefinitionImpl activityDefinition: activityDefinitions) {
        activityDefinition.setProcessEngine(processEngine);
        activityDefinition.setProcessDefinition(processDefinition);
        activityDefinition.setParent(this);
        Exceptions.checkNotNull(activityDefinition.getId(), "activityDefinition.id");
        activityDefinitionsMap.put(activityDefinition.getId(), activityDefinition);
        activityDefinition.prepare();
      }
    }
    if (transitionDefinitions!=null) {
      transitionDefinitionsMap = new HashMap<>();
      for (TransitionDefinitionImpl transitionDefinition: transitionDefinitions) {
        if (startActivityDefinitions!=null) {
          startActivityDefinitions.remove(transitionDefinition.getTo());
        }
        transitionDefinition.setProcessEngine(processEngine);
        transitionDefinition.setProcessDefinition(processDefinition);
        transitionDefinition.setParent(this);
        Exceptions.checkNotNull(transitionDefinition.getId(), "transitionDefinition.id");
        transitionDefinitionsMap.put(transitionDefinition.getId(), transitionDefinition);
        transitionDefinition.prepare();
      }
    }
    if (variableDefinitions!=null) {
      variableDefinitionsMap = new HashMap<>();
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        variableDefinition.setProcessEngine(processEngine);
        variableDefinition.setProcessDefinition(processDefinition);
        variableDefinition.setParent(this);
        Exceptions.checkNotNull(variableDefinition.getId(), "variableDefinition.id");
        variableDefinitionsMap.put(variableDefinition.getId(), variableDefinition);
        variableDefinition.prepare();
      }
    }
    if (parameterInstances!=null) {
      parameterInstancesMap = new HashMap<>();
      for (ParameterInstanceImpl parameterInstance: parameterInstances) {
        parameterInstance.setProcessEngine(processEngine);
        String name = parameterInstance.getName();
        Exceptions.checkNotNull(name, "parameterInstance.name");
        parameterInstancesMap.put(name, parameterInstance);
        parameterInstance.prepare();
      }
    }
  } 
  
  public ParameterInstanceImpl findParameterInstance(String parameterRefId) {
    return parameterInstancesMap.get(parameterRefId);
  }

  
  public abstract ProcessDefinitionPathImpl getPath();

  public List<ActivityDefinitionImpl> getStartActivityDefinitions() {
    return startActivityDefinitions;
  }
  
  public void setStartActivityDefinitions(List<ActivityDefinitionImpl> startActivityDefinitions) {
    this.startActivityDefinitions = startActivityDefinitions;
  }

  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  public ActivityDefinitionImpl getActivityDefinition(ActivityDefinitionId id) {
    return activityDefinitionsMap!=null ? activityDefinitionsMap.get(id) : null;
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

  public VariableDefinitionImpl getVariableDefinition(VariableDefinitionId id) {
    return variableDefinitionsMap!=null ? variableDefinitionsMap.get(id) : null;
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
  
  public boolean hasTransitionDefinitions() {
    return transitionDefinitions!=null && !transitionDefinitions.isEmpty();
  } 
  
  public TransitionDefinitionImpl getTransitionDefinition(TransitionDefinitionId id) {
    return transitionDefinitionsMap!=null ? transitionDefinitionsMap.get(id) : null;
  }

  
  public List<ActivityDefinitionImpl> getActivityDefinitions() {
    return activityDefinitions;
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

  
  public Map<TransitionDefinitionId, TransitionDefinitionImpl> getTransitionDefinitionsMap() {
    return transitionDefinitionsMap;
  }

  
  public void setTransitionDefinitionsMap(Map<TransitionDefinitionId, TransitionDefinitionImpl> transitionDefinitionsMap) {
    this.transitionDefinitionsMap = transitionDefinitionsMap;
  }

  public void visit(ProcessDefinitionVisitor visitor) {
    visitor.visitCompositeDefinition(this);
  }

  public void notifyActivityInstanceEnded(ActivityInstanceImpl activityInstance) {
    ScopeInstanceImpl parentCompositeInstance = activityInstance.getParent();
    if (!parentCompositeInstance.hasUnfinishedActivityInstances()) {
      parentCompositeInstance.end();
    }
  }
  public boolean containsVariable(VariableDefinitionId variableDefinitionId) {
    if (variableDefinitionId==null) {
      return false;
    }
    if (variableDefinitions!=null) {
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        if (variableDefinitionId.equals(variableDefinition.getId())) {
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
}

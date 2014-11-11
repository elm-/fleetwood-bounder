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

import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ScopeInstance;
import fleetwood.bounder.instance.ProcessEngineImpl;
import fleetwood.bounder.util.Exceptions;
import fleetwood.bounder.util.Identifyable;


/**
 * @author Walter White
 */
public abstract class ScopeDefinition implements Identifyable {

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinition processDefinition;
  protected ScopeDefinition parent;
  protected List<ActivityDefinition> activityDefinitions;
  protected Map<ActivityDefinitionId, ActivityDefinition> activityDefinitionsMap;
  protected List<VariableDefinition> variableDefinitions;
  protected Map<VariableDefinitionId, VariableDefinition> variableDefinitionsMap;
  protected List<TransitionDefinition> transitionDefinitions;
  protected Map<TransitionDefinitionId, TransitionDefinition> transitionDefinitionsMap;
  protected List<ParameterInstance> parameterInstances;
  protected Map<String, ParameterInstance> parameterInstancesMap;

  protected List<ActivityDefinition> startActivityDefinitions;
  
  public  ScopeDefinition parameterObject(ParameterDefinition parameterDefinition, Object object) {
    addParameterValue(parameterDefinition, new ParameterValue().object(object));
    return this;
  }
  public  ScopeDefinition parameterExpression(ParameterDefinition parameterDefinition, String expression) {
    addParameterValue(parameterDefinition, new ParameterValue().expression(expression));
    return this;
  }
  public  ScopeDefinition parameterVariable(ParameterDefinition parameterDefinition, VariableDefinition variableDefinition) {
    addParameterValue(parameterDefinition, new ParameterValue().variableDefinition(variableDefinition));
    return this;
  }

   void addParameterValue(ParameterDefinition parameterDefinition, ParameterValue parameterValue) {
    Exceptions.checkNotNull(parameterDefinition, "parameterDefinition");
    ParameterInstance parameterInstance = findParameterInstance(parameterDefinition.getName());
    if (parameterInstance==null) {
      parameterInstance = new ParameterInstance();
      parameterInstance.setName(parameterDefinition.getName());
      parameterInstance.setParameterDefinition((ParameterDefinition)parameterDefinition);
      parameterInstances.add(parameterInstance);
    }
    parameterInstance.addParameterValue(parameterValue);
  }

  public ParameterDefinitions getParameterDefinitions() {
    return null;
  }

  ParameterInstance findParameterInstance(String parameterName) {
    if (parameterInstances!=null) {
      for (ParameterInstance parameterInstance: parameterInstances) {
        if (parameterName.equals(parameterInstance.getName())) {
          return parameterInstance;
        }
      }
    }
    return null;
  }
  
  
  /** performs initializations after the activity is constructed and before the process is used in execution.
   * eg calculating the start activities */ 
  public void prepare() {
    if (activityDefinitions!=null) {
      startActivityDefinitions = new ArrayList<>(activityDefinitions);
      activityDefinitionsMap = new HashMap<>();
      for (ActivityDefinition activityDefinition: activityDefinitions) {
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
      for (TransitionDefinition transitionDefinition: transitionDefinitions) {
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
      for (VariableDefinition variableDefinition: variableDefinitions) {
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
      for (ParameterInstance parameterInstance: parameterInstances) {
        parameterInstance.setProcessEngine(processEngine);
        String name = parameterInstance.getName();
        Exceptions.checkNotNull(name, "parameterInstance.name");
        parameterInstancesMap.put(name, parameterInstance);
        parameterInstance.prepare();
      }
    }
  } 
  
  public abstract ProcessDefinitionPath getPath();

  public List<ActivityDefinition> getStartActivityDefinitions() {
    return startActivityDefinitions;
  }
  
  public void setStartActivityDefinitions(List<ActivityDefinition> startActivityDefinitions) {
    this.startActivityDefinitions = startActivityDefinitions;
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
  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public ScopeDefinition getParent() {
    return parent;
  }

  public void setParent(ScopeDefinition parent) {
    this.parent = parent;
  }
  
  public boolean isProcessDefinition() {
    return parent!=null;
  }

  public <T extends ActivityDefinition> T addActivityDefinition(T activityDefinition) {
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

  public VariableDefinition getVariableDefinition(VariableDefinitionId id) {
    return variableDefinitionsMap!=null ? variableDefinitionsMap.get(id) : null;
  }
  
  public  ScopeDefinition addVariableDefinition(VariableDefinition variableDefinition) {
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

  public void createTransitionDefinition(ActivityDefinition from, ActivityDefinition to) {
    TransitionDefinition transitionDefinition = new TransitionDefinition();
    transitionDefinition.setFrom(from);
    transitionDefinition.setTo(to);
    addTransitionDefinition(transitionDefinition);
    from.addOutgoingTransition(transitionDefinition);
  }

  public ScopeDefinition addTransitionDefinition(TransitionDefinition transitionDefinition) {
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

  public void visit(ProcessDefinitionVisitor visitor) {
    visitor.visitCompositeDefinition(this);
  }

  public void notifyActivityInstanceEnded(ActivityInstance activityInstance) {
    ScopeInstance parentCompositeInstance = activityInstance.getParent();
    if (!parentCompositeInstance.hasUnfinishedActivityInstances()) {
      parentCompositeInstance.end();
    }
  }
}

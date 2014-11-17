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

import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.ScopeInstanceImpl;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public abstract class ScopeDefinitionImpl {

  // parsed and stored member fields

  public String name;
  public List<ActivityDefinitionImpl> activityDefinitions;
  public List<VariableDefinitionImpl> variableDefinitions;
  public List<TransitionDefinitionImpl> transitionDefinitions;
  public List<TimerDefinitionImpl> timerDefinitions;

  // derived fields that are initialized in the prepare() method

  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;
  public ScopeDefinitionImpl parent;
  public List<ActivityDefinitionImpl> startActivityDefinitions;
  public Map<String, ActivityDefinitionImpl> activityDefinitionsMap;
  public Map<String, VariableDefinitionImpl> variableDefinitionsMap;

  public Long buildLine;
  public Long buildColumn;
  
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

  public ScopeDefinitionImpl name(String name) {
    this.name = name;
    return this;
  }

  public ScopeDefinitionImpl line(Long line) {
    this.buildLine = line;
    return this;
  }

  public ScopeDefinitionImpl column(Long column) {
    this.buildColumn = column;
    return this;
  }

  public void parse(ParseContext parseContext) {
    if (variableDefinitions!=null) {
      variableDefinitionsMap = new HashMap<>();
      for (int i=0; i<variableDefinitions.size(); i++) {
        VariableDefinitionImpl variableDefinition = variableDefinitions.get(i);
        parseContext.pushPathElement(variableDefinition, variableDefinition.name, i);
        variableDefinition.validate(parseContext);
        parseContext.popPathElement();
        variableDefinitionsMap.put(variableDefinition.name, variableDefinition);
      }
    }
    if (activityDefinitions!=null) {
      activityDefinitionsMap = new HashMap<>();
      for (int i=0; i<activityDefinitions.size(); i++) {
        ActivityDefinitionImpl activityDefinition = activityDefinitions.get(i);
        parseContext.pushPathElement(activityDefinition, activityDefinition.name, i);
        activityDefinition.parse(parseContext);
        parseContext.popPathElement();
        activityDefinitionsMap.put(activityDefinition.name, activityDefinition);
      }
    }
    if (transitionDefinitions!=null) {
      for (int i=0; i<transitionDefinitions.size(); i++) {
        TransitionDefinitionImpl transitionDefinition = transitionDefinitions.get(i);
        parseContext.pushPathElement(transitionDefinition, transitionDefinition.name, i);
        transitionDefinition.validate(parseContext);
        parseContext.popPathElement();
      }
    }
    if (timerDefinitions!=null) {
      for (int i=0; i<timerDefinitions.size(); i++) {
        TimerDefinitionImpl timerDefinition = timerDefinitions.get(i);
        parseContext.pushPathElement(timerDefinition, timerDefinition.name, i);
        timerDefinition.validate(parseContext);
        parseContext.popPathElement();
      }
    }
  }


  /// Process Definition Parsing methods //////////////////////////////////////////

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
        activityDefinitionsMap.put(activityDefinition.name, activityDefinition);
        activityDefinition.prepare();
      }
    }
    if (transitionDefinitions!=null) {
      for (TransitionDefinitionImpl transitionDefinition: transitionDefinitions) {
        if (startActivityDefinitions!=null) {
          startActivityDefinitions.remove(transitionDefinition.getTo());
        }
        transitionDefinition.setProcessEngine(processEngine);
        transitionDefinition.setProcessDefinition(processDefinition);
        transitionDefinition.setParent(this);
        transitionDefinition.prepare();
      }
    }
    if (variableDefinitions!=null) {
      variableDefinitionsMap = new HashMap<>();
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        variableDefinition.setProcessEngine(processEngine);
        variableDefinition.setProcessDefinition(processDefinition);
        variableDefinition.setParent(this);
        variableDefinitionsMap.put(variableDefinition.name, variableDefinition);
        variableDefinition.prepare();
      }
    }
  } 
  
  public abstract ProcessDefinitionPath getPath();

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
  
  public ActivityDefinitionImpl getActivityDefinition(String name) {
    return activityDefinitionsMap!=null ? activityDefinitionsMap.get(name) : null;
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
  
  public boolean hasTransitionDefinitions() {
    return transitionDefinitions!=null && !transitionDefinitions.isEmpty();
  } 
  
  public List<ActivityDefinitionImpl> getActivityDefinitions() {
    return activityDefinitions;
  }

  
  public void setActivityDefinitions(List<ActivityDefinitionImpl> activityDefinitions) {
    this.activityDefinitions = activityDefinitions;
  }

  
  public Map<String, ActivityDefinitionImpl> getActivityDefinitionsMap() {
    return activityDefinitionsMap;
  }

  
  public void setActivityDefinitionsMap(Map<String, ActivityDefinitionImpl> activityDefinitionsMap) {
    this.activityDefinitionsMap = activityDefinitionsMap;
  }

  
  public List<VariableDefinitionImpl> getVariableDefinitions() {
    return variableDefinitions;
  }

  
  public void setVariableDefinitions(List<VariableDefinitionImpl> variableDefinitions) {
    this.variableDefinitions = variableDefinitions;
  }

  
  public Map<String, VariableDefinitionImpl> getVariableDefinitionsMap() {
    return variableDefinitionsMap;
  }

  
  public void setVariableDefinitionsMap(Map<String, VariableDefinitionImpl> variableDefinitionsMap) {
    this.variableDefinitionsMap = variableDefinitionsMap;
  }

  
  public List<TransitionDefinitionImpl> getTransitionDefinitions() {
    return transitionDefinitions;
  }

  
  public void setTransitionDefinitions(List<TransitionDefinitionImpl> transitionDefinitions) {
    this.transitionDefinitions = transitionDefinitions;
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
  public boolean containsVariable(String variableDefinitionName) {
    if (variableDefinitionName==null) {
      return false;
    }
    if (variableDefinitions!=null) {
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        if (variableDefinitionName.equals(variableDefinition.name)) {
          return true;
        }
      }
    }
    ScopeDefinitionImpl parent = getParent();
    if (parent!=null) {
      return parent.containsVariable(variableDefinitionName);
    }
    return false;
  }

  public VariableDefinitionImpl findVariableDefinitionByName(String variableDefinitionName) {
    return variableDefinitionsMap!=null ? variableDefinitionsMap.get(variableDefinitionName) : null;
  }
}

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
package com.heisenberg.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.ProcessEngine;
import com.heisenberg.definition.ActivityDefinition;
import com.heisenberg.definition.ProcessDefinition;
import com.heisenberg.definition.ScopeDefinition;
import com.heisenberg.definition.VariableDefinition;
import com.heisenberg.definition.VariableDefinitionId;
import com.heisenberg.engine.updates.ActivityInstanceCreateUpdate;
import com.heisenberg.instance.json.Jsonnable;
import com.heisenberg.type.Type;
import com.heisenberg.type.TypedValue;
import com.heisenberg.util.Identifyable;
import com.heisenberg.util.Time;


/**
 * @author Walter White
 */
public abstract class ScopeInstance implements Identifyable, Jsonnable {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  protected Long start;
  
  protected Long end;

  protected Long duration;

  protected List<ActivityInstance> activityInstances;

  protected List<VariableInstance> variableInstances;
  protected Map<VariableDefinitionId, VariableInstance> variableInstancesMap;

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinition processDefinition;
  protected ScopeDefinition scopeDefinition;
  protected ProcessInstance processInstance;
  protected ScopeInstance parent;

  public ActivityInstance createActivityInstance(ActivityDefinition activityDefinition) {
    ActivityInstance activityInstance = processEngine.createActivityInstance(activityDefinition);
    activityInstance.setProcessEngine(processEngine);
    activityInstance.setScopeDefinition(activityDefinition);
    activityInstance.setProcessInstance(processInstance);
    activityInstance.setParent(this);
    activityInstance.setActivityDefinition(activityDefinition);
    activityInstance.setStart(Time.now());
    if (activityInstances==null) {
      activityInstances = new ArrayList<>();
    }
    activityInstances.add(activityInstance);
    activityInstance.initializeVariableInstances();
    log.debug("Created "+activityInstance);
    processInstance.addUpdate(new ActivityInstanceCreateUpdate(activityInstance));
    return activityInstance;
  }
  
  protected void initializeVariableInstances() {
    List<VariableDefinition> variableDefinitions = scopeDefinition.getVariableDefinitions();
    if (variableDefinitions!=null) {
      for (VariableDefinition variableDefinition: variableDefinitions) {
        VariableInstance variableInstance = variableDefinition.createVariableInstance();
        variableInstance.setProcessEngine(processEngine);
        variableInstance.setParent(this);
        variableInstance.setProcessInstance(processInstance);
        if (variableInstances==null) {
          variableInstances = new ArrayList<>();
        }
        variableInstances.add(variableInstance);
      }
    }
  }
  
  public void setVariableValuesRecursive(Map<VariableDefinitionId, Object> variableValues) {
    if (variableValues!=null) {
      for (VariableDefinitionId variableDefinitionId: variableValues.keySet()) {
        Object value = variableValues.get(variableDefinitionId);
        setVariableValueRecursive(variableDefinitionId, value);
      }
    }
  }

  public void setVariableValueRecursive(VariableDefinitionId variableDefinitionId, Object value) {
    if (variableInstances!=null) {
      VariableInstance variableInstance = getVariableInstanceLocal(variableDefinitionId);
      if (variableInstance!=null) {
        variableInstance.setValue(value);
      }
    }
    if (parent!=null) {
      parent.setVariableValueRecursive(variableDefinitionId, value);
    }
  }
  
  public TypedValue getVariableValueRecursive(VariableDefinitionId variableDefinitionId) {
    if (variableInstances!=null) {
      VariableInstance variableInstance = getVariableInstanceLocal(variableDefinitionId);
      if (variableInstance!=null) {
        Type type = variableInstance.getVariableDefinition().getType();
        Object value = variableInstance.getValue();
        return new TypedValue(type, value);
      }
    }
    if (parent!=null) {
      return parent.getVariableValueRecursive(variableDefinitionId);
    }
    return null;
  }
  
  protected VariableInstance getVariableInstanceLocal(VariableDefinitionId variableDefinitionId) {
    ensureVariableInstancesMapInitialized();
    return variableInstancesMap.get(variableDefinitionId);
  }

  protected void ensureVariableInstancesMapInitialized() {
    if (variableInstancesMap==null && variableInstances!=null) {
      variableInstancesMap = new HashMap<>();
      for (VariableInstance variableInstance: variableInstances) {
        variableInstancesMap.put(variableInstance.getVariableDefinition().getId(), variableInstance);
      }
    }
  }
  
  public abstract void end();

  public boolean hasUnfinishedActivityInstances() {
    if (activityInstances==null) {
      return false;
    }
    for (ActivityInstance activityInstance: activityInstances) {
      if (!activityInstance.isEnded()) {
        return true;
      }
    }
    return false;
  }

  
  /** searches for the variable starting in this activity and upwards over the parent hierarchy */ 
  public void setVariableByName(String variableName, Object value) {
  }

  /** scans this activity and the nested activities */
  public ActivityInstance findActivityInstance(ActivityInstanceId activityInstanceId) {
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance: activityInstances) {
        ActivityInstance theOne = activityInstance.findActivityInstance(activityInstanceId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }
  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  public ScopeDefinition getScopeDefinition() {
    return scopeDefinition;
  }

  public void setScopeDefinition(ScopeDefinition scopeDefinition) {
    this.scopeDefinition = scopeDefinition;
  }
  
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }
  
  public List<ActivityInstance> getActivityInstances() {
    return activityInstances;
  }
  
  public void setActivityInstances(List<ActivityInstance> activityInstances) {
    this.activityInstances = activityInstances;
  }

  public boolean hasActivityInstances() {
    return activityInstances!=null && !activityInstances.isEmpty();
  }
  
  public ScopeInstance getParent() {
    return parent;
  }
  
  public void setParent(ScopeInstance parent) {
    this.parent = parent;
  }

  public Long getStart() {
    return start;
  }
  
  public void setStart(Long start) {
    this.start = start;
  }
  
  public Long getEnd() {
    return end;
  }
  
  public abstract void setEnd(Long end); 
  
  public boolean isEnded() {
    return end!=null;
  }

  
  public Long getDuration() {
    return duration;
  }
  
  public void setDuration(Long duration) {
    this.duration = duration;
  }
  
  public List<VariableInstance> getVariableInstances() {
    return variableInstances;
  }
  
  public void setVariableInstances(List<VariableInstance> variableInstances) {
    this.variableInstances = variableInstances;
  }
}

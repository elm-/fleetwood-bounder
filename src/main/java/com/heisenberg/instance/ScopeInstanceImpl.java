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

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ScopeInstance;
import com.heisenberg.api.instance.VariableInstance;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.ScopeDefinitionImpl;
import com.heisenberg.definition.VariableDefinitionImpl;
import com.heisenberg.engine.updates.ActivityInstanceCreateUpdate;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.json.Jsonnable;
import com.heisenberg.spi.Type;
import com.heisenberg.type.TypedValue;
import com.heisenberg.util.Identifyable;
import com.heisenberg.util.Time;


/**
 * @author Walter White
 */
public abstract class ScopeInstanceImpl implements Identifyable, Jsonnable {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public Long start;
  public Long end;
  public Long duration;
  public List<ActivityInstanceImpl> activityInstances;

  public List<VariableInstanceImpl> variableInstances;
  public Map<String, VariableInstanceImpl> variableInstancesMap;

  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;
  public ScopeDefinitionImpl scopeDefinition;
  public ProcessInstanceImpl processInstance;
  public ScopeInstanceImpl parent;
  
  
  public void serialize(ScopeInstance scopeInstance) {
    scopeInstance.start = start;
    scopeInstance.end = end;
    scopeInstance.duration = duration;
    if (activityInstances!=null) {
      scopeInstance.activityInstances = new ArrayList<>(activityInstances.size());
      for (ActivityInstanceImpl activityInstanceImpl: activityInstances) {
        ActivityInstance activityInstance = activityInstanceImpl.serializeToJson();
        scopeInstance.activityInstances.add(activityInstance);
      }
    }
    if (variableInstances!=null) {
      scopeInstance.variableInstances = new ArrayList<>(variableInstances.size());
      for (VariableInstanceImpl variableInstanceImpl: variableInstances) {
        VariableInstance variableInstance = variableInstanceImpl.serialize();
        scopeInstance.variableInstances.add(variableInstance);
      }
    }
  }

  public ActivityInstanceImpl createActivityInstance(ActivityDefinitionImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = processEngine.createActivityInstance(activityDefinition);
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
    List<VariableDefinitionImpl> variableDefinitions = scopeDefinition.getVariableDefinitions();
    if (variableDefinitions!=null) {
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        VariableInstanceImpl variableInstance = variableDefinition.createVariableInstance();
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
  
  public void setVariableValuesRecursive(Map<String, Object> variableValues) {
    if (variableValues!=null) {
      for (String variableDefinitionId: variableValues.keySet()) {
        Object value = variableValues.get(variableDefinitionId);
        setVariableValueRecursive(variableDefinitionId, value);
      }
    }
  }

  public void setVariableValueRecursive(String variableDefinitionName, Object value) {
    if (variableInstances!=null) {
      VariableInstanceImpl variableInstance = getVariableInstanceLocal(variableDefinitionName);
      if (variableInstance!=null) {
        variableInstance.setValue(value);
      }
    }
    if (parent!=null) {
      parent.setVariableValueRecursive(variableDefinitionName, value);
    }
  }
  
  public TypedValue getVariableValueRecursive(String variableDefinitionName) {
    if (variableInstances!=null) {
      VariableInstanceImpl variableInstance = getVariableInstanceLocal(variableDefinitionName);
      if (variableInstance!=null) {
        Type type = variableInstance.getVariableDefinition().getType();
        Object value = variableInstance.getValue();
        return new TypedValue(type, value);
      }
    }
    if (parent!=null) {
      return parent.getVariableValueRecursive(variableDefinitionName);
    }
    throw new RuntimeException("Variable "+variableDefinitionName+" is not defined in "+getClass().getSimpleName()+" "+getId());
  }
  
  protected VariableInstanceImpl getVariableInstanceLocal(String variableDefinitionName) {
    ensureVariableInstancesMapInitialized();
    return variableInstancesMap.get(variableDefinitionName);
  }

  protected void ensureVariableInstancesMapInitialized() {
    if (variableInstancesMap==null && variableInstances!=null) {
      variableInstancesMap = new HashMap<>();
      for (VariableInstanceImpl variableInstance: variableInstances) {
        variableInstancesMap.put(variableInstance.variableDefinition.name, variableInstance);
      }
    }
  }
  
  public abstract void end();

  public boolean hasUnfinishedActivityInstances() {
    if (activityInstances==null) {
      return false;
    }
    for (ActivityInstanceImpl activityInstance: activityInstances) {
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
  public ActivityInstanceImpl findActivityInstance(ActivityInstanceId activityInstanceId) {
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        ActivityInstanceImpl theOne = activityInstance.findActivityInstance(activityInstanceId);
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

  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  public ScopeDefinitionImpl getScopeDefinition() {
    return scopeDefinition;
  }

  public void setScopeDefinition(ScopeDefinitionImpl scopeDefinition) {
    this.scopeDefinition = scopeDefinition;
  }
  
  public ProcessInstanceImpl getProcessInstance() {
    return processInstance;
  }
  
  public void setProcessInstance(ProcessInstanceImpl processInstance) {
    this.processInstance = processInstance;
  }
  
  public List<ActivityInstanceImpl> getActivityInstances() {
    return activityInstances;
  }
  
  public void setActivityInstances(List<ActivityInstanceImpl> activityInstances) {
    this.activityInstances = activityInstances;
  }

  public boolean hasActivityInstances() {
    return activityInstances!=null && !activityInstances.isEmpty();
  }
  
  public ScopeInstanceImpl getParent() {
    return parent;
  }
  
  public void setParent(ScopeInstanceImpl parent) {
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
  
  public List<VariableInstanceImpl> getVariableInstances() {
    return variableInstances;
  }
  
  public void setVariableInstances(List<VariableInstanceImpl> variableInstances) {
    this.variableInstances = variableInstances;
  }
}

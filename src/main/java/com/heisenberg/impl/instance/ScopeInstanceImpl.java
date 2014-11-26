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
package com.heisenberg.impl.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ScopeInstance;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.util.TypedValue;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ScopeDefinitionImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.engine.operation.StartActivityInstanceOperation;
import com.heisenberg.impl.engine.updates.ActivityInstanceCreateUpdate;


/**
 * @author Walter White
 */
public abstract class ScopeInstanceImpl implements ScopeInstance {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public Object id;
  public LocalDateTime start;
  public LocalDateTime end;
  public Long duration;
  public List<ActivityInstanceImpl> activityInstances;
  public List<VariableInstanceImpl> variableInstances;

  @JsonIgnore
  public Map<Object, VariableInstanceImpl> variableInstancesMap;

  @JsonIgnore
  public ProcessEngineImpl processEngine;
  @JsonIgnore
  public ProcessDefinitionImpl processDefinition;
  @JsonIgnore
  public ScopeDefinitionImpl scopeDefinition;
  @JsonIgnore
  public ProcessInstanceImpl processInstance;
  @JsonIgnore
  public ScopeInstanceImpl parent;
  
  protected void visitCompositeInstance(ProcessInstanceVisitor visitor) {
    visitActivityInstances(visitor);
    visitVariableInstances(visitor);
  }

  protected void visitActivityInstances(ProcessInstanceVisitor visitor) {
    if (activityInstances!=null) {
      for (int i=0; i<activityInstances.size(); i++) {
        ActivityInstanceImpl activityInstance = activityInstances.get(i);
        visitor.startActivityInstance(activityInstance, i);
        activityInstance.visit(visitor, i);
        visitor.endActivityInstance(activityInstance, i);
      }
    }
  }
  
  protected void visitVariableInstances(ProcessInstanceVisitor visitor) {
    if (variableInstances!=null) {
      for (int i=0; i<variableInstances.size(); i++) {
        VariableInstanceImpl variableInstance = variableInstances.get(i);
        visitor.variableInstance(variableInstance, i);
      }
    }
  }
  
  public void start(ActivityDefinition activityDefinition) {
    ActivityInstanceImpl activityInstance = createActivityInstance((ActivityDefinitionImpl) activityDefinition);
    processInstance.addOperation(new StartActivityInstanceOperation(activityInstance));
  }

  public ActivityInstanceImpl createActivityInstance(ActivityDefinitionImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = processEngine.createActivityInstance(this, activityDefinition);
    activityInstance.processEngine = processEngine;
    activityInstance.scopeDefinition = activityDefinition;
    activityInstance.processInstance = processInstance;
    activityInstance.activityDefinition = activityDefinition;
    activityInstance.activityDefinitionId = activityDefinition.id;
    activityInstance.setStart(Time.now());
    addActivityInstance(activityInstance);
    processInstance.addUpdate(new ActivityInstanceCreateUpdate(activityInstance));
    activityInstance.initializeVariableInstances();
    log.debug("Created "+activityInstance);
    return activityInstance;
  }

  public void addActivityInstance(ActivityInstanceImpl activityInstance) {
    if (activityInstances==null) {
      activityInstances = new ArrayList<>();
    }
    activityInstance.parent = this;
    activityInstances.add(activityInstance);
  }
  
  protected void initializeVariableInstances() {
    List<VariableDefinitionImpl> variableDefinitions = scopeDefinition.getVariableDefinitions();
    if (variableDefinitions!=null) {
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        VariableInstanceImpl variableInstance = processEngine.createVariableInstance(this, variableDefinition);
        variableInstance.processEngine = processEngine;
        variableInstance.processInstance = processInstance;
        variableInstance.dataType = variableDefinition.dataType;
        variableInstance.value = variableDefinition.initialValue;
        variableInstance.variableDefinition = variableDefinition;
        variableInstance.variableDefinitionId = variableDefinition.id;
        addVariableInstance(variableInstance);
      }
    }
  }

  public void addVariableInstance(VariableInstanceImpl variableInstance) {
    variableInstance.parent = this;
    if (variableInstances==null) {
      variableInstances = new ArrayList<>();
    }
    variableInstances.add(variableInstance);
  }
  
  public void setVariableValuesRecursive(Map<Object, Object> variableValues) {
    if (variableValues!=null) {
      for (Object variableDefinitionId: variableValues.keySet()) {
        Object value = variableValues.get(variableDefinitionId);
        setVariableValueRecursive(variableDefinitionId, value);
      }
    }
  }

  public void setVariableValueRecursive(Object variableDefinitionName, Object value) {
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
  
  public TypedValue getVariableValueRecursive(Object variableDefinitionId) {
    if (variableInstances!=null) {
      VariableInstanceImpl variableInstance = getVariableInstanceLocal(variableDefinitionId);
      if (variableInstance!=null) {
        DataType dataType = variableInstance.getVariableDefinition().getType();
        Object value = variableInstance.getValue();
        return new TypedValue(dataType, value);
      }
    }
    if (parent!=null) {
      return parent.getVariableValueRecursive(variableDefinitionId);
    }
    throw new RuntimeException("Variable "+variableDefinitionId+" is not defined in "+getClass().getSimpleName()+" "+toString());
  }
  
  protected VariableInstanceImpl getVariableInstanceLocal(Object variableDefinitionId) {
    ensureVariableInstancesMapInitialized();
    return variableInstancesMap.get(variableDefinitionId);
  }

  protected void ensureVariableInstancesMapInitialized() {
    if (variableInstancesMap==null && variableInstances!=null) {
      variableInstancesMap = new HashMap<>();
      for (VariableInstanceImpl variableInstance: variableInstances) {
        variableInstancesMap.put(variableInstance.variableDefinition.id, variableInstance);
      }
    }
  }
  
  public abstract void end();

  public boolean hasOpenActivityInstances() {
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
  public ActivityInstanceImpl findActivityInstance(Object activityInstanceId) {
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
  
  @Override
  public ActivityInstanceImpl findActivityInstanceByName(String activityDefinitionName) {
    if (activityDefinitionName==null) {
      return null;
    }
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        ActivityInstanceImpl theOne = activityInstance.findActivityInstanceByName(activityDefinitionName);
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

  public LocalDateTime getStart() {
    return start;
  }
  
  public void setStart(LocalDateTime start) {
    this.start = start;
  }
  
  public LocalDateTime getEnd() {
    return end;
  }
  
  public abstract void setEnd(LocalDateTime end); 
  
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

  public Object getId() {
    return id;
  }

  public void setId(Object id) {
    this.id = id;
  }

  public abstract void ended(ActivityInstanceImpl activityInstance);
}

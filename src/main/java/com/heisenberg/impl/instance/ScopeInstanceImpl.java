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

import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING;
import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_CONTAINER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.api.instance.WorkflowInstanceEventListener;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.api.instance.ScopeInstance;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.definition.ActivityImpl;
import com.heisenberg.impl.definition.ScopeImpl;
import com.heisenberg.impl.definition.VariableImpl;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.plugin.Binding;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.impl.plugin.TypedValue;
import com.heisenberg.impl.script.ScriptResult;
import com.heisenberg.impl.script.ScriptService;
import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public abstract class ScopeInstanceImpl implements ScopeInstance {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public String id;
  public LocalDateTime start;
  public LocalDateTime end;
  public Long duration;
  public List<ActivityInstanceImpl> activityInstances;
  public List<VariableInstanceImpl> variableInstances;

  @JsonIgnore
  public Map<Object, VariableInstanceImpl> variableInstancesMap;
  @JsonIgnore
  public WorkflowEngineImpl workflowEngine;
  @JsonIgnore
  public WorkflowImpl workflow;
  @JsonIgnore
  public ScopeImpl scopeDefinition;
  @JsonIgnore
  public WorkflowInstanceImpl workflowInstance;
  @JsonIgnore
  public ScopeInstanceImpl parent;
  @JsonIgnore
  // As long as the workflow instance is not saved, the updates collection is null.
  // That means it's not yet necessary to collect the updates. 
  public ScopeInstanceUpdates updates;
  
  public abstract void setEnd(LocalDateTime end); 
  
  public abstract void ended(ActivityInstanceImpl activityInstance);

  public abstract boolean isProcessInstance();

  public void start(Activity activity) {
    createActivityInstance((ActivityImpl) activity);
  }

  public ActivityInstanceImpl createActivityInstance(ActivityImpl activity) {
    ActivityInstanceImpl activityInstance = workflowEngine.createActivityInstance(this, activity);
    activityInstance.workflowEngine = workflowEngine;
    activityInstance.scopeDefinition = activity;
    activityInstance.workflowInstance = workflowInstance;
    activityInstance.activityDefinition = activity;
    activityInstance.activityId = activity.id;
    if (activity.isMultiInstance()) {
      activityInstance.setWorkState(STATE_STARTING_MULTI_CONTAINER);
    } else {
      activityInstance.setWorkState(STATE_STARTING);
    }
    workflowInstance.addWork(activityInstance);
    activityInstance.setStart(Time.now());
    // TODO: this should correspond with the actual start method call, e.g. when the OP is executed (set time as well)?
    for (WorkflowInstanceEventListener listener : getWorkflowEngine().getListeners()) {
      listener.started(activityInstance);
    }
    if (updates!=null) {
      activityInstance.updates = new ActivityInstanceUpdates(true);
      propagateActivityInstanceChange(this);
    }
    addActivityInstance(activityInstance);
    activityInstance.initializeVariableInstances();
    log.debug("Created "+activityInstance);
    return activityInstance;
  }
  
  public void initializeForEachElement(VariableImpl elementVariableDefinition, Object value) {
    VariableInstanceImpl elementVariableInstance = createVariableInstance(elementVariableDefinition);
    elementVariableInstance.setValue(value);
  }

  public void addActivityInstance(ActivityInstanceImpl activityInstance) {
    if (activityInstances==null) {
      activityInstances = new ArrayList<>();
    }
    activityInstance.parent = this;
    activityInstances.add(activityInstance);
  }
  
  public void initializeVariableInstances() {
    List<VariableImpl> variableDefinitions = scopeDefinition.getVariableDefinitions();
    if (variableDefinitions!=null) {
      for (VariableImpl variableDefinition: variableDefinitions) {
        createVariableInstance(variableDefinition);
      }
    }
  }

  public VariableInstanceImpl createVariableInstance(VariableImpl variableDefinition) {
    VariableInstanceImpl variableInstance = workflowEngine.createVariableInstance(this, variableDefinition);
    variableInstance.processEngine = workflowEngine;
    variableInstance.processInstance = workflowInstance;
    variableInstance.dataType = variableDefinition.dataType;
    variableInstance.value = variableDefinition.initialValue;
    variableInstance.variableDefinition = variableDefinition;
    variableInstance.variableDefinitionId = variableDefinition.id;
    if (updates!=null) {
      variableInstance.updates = new VariableInstanceUpdates(true);
      updates.isVariableInstancesChanged = true;
      propagateActivityInstanceChange(this);
    }
    addVariableInstance(variableInstance);
    return variableInstance;
  }

  public void addVariableInstance(VariableInstanceImpl variableInstance) {
    variableInstance.parent = this;
    if (variableInstances==null) {
      variableInstances = new ArrayList<>();
    }
    variableInstances.add(variableInstance);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getValue(Binding<T> binding) {
    if (binding==null) {
      return null;
    }
    if (!binding.isInitialized) {
      throw new RuntimeException("Binding "+binding+" in "+scopeDefinition+" is not initialized");
    }
    if (binding.value!=null) {
      return (T) binding.value;
    }
    if (binding.variableDefinitionId!=null) {
      return (T) getVariableValue(binding.variableDefinitionId);
    }
    if (binding.expressionScript!=null) {
      ScriptService scriptService = getServiceRegistry().getService(ScriptService.class);
      ScriptResult scriptResult = scriptService.evaluateScript(this, binding.expressionScript);
      Object result = scriptResult.getResult();
      return (T) binding.dataType.convertScriptValueToInternal(result, binding.expressionScript.language);
    }
    return null;
  }

  public <T> List<T> getValue(List<Binding<T>> bindings) {
    List<T> list = new ArrayList<>();
    if (bindings!=null) {
      for (Binding<T> binding: bindings) {
        list.add(getValue(binding));
      }
    }
    return list;
  }

  public Object getVariableValue(String variableDefinitionId) {
    TypedValue typedValue = getVariableTypedValue(variableDefinitionId);
    return typedValue!=null ? typedValue.getValue() : null;
  }

  public void setVariableValues(Map<String, Object> variableValues) {
    if (variableValues!=null) {
      for (String variableDefinitionId: variableValues.keySet()) {
        Object value = variableValues.get(variableDefinitionId);
        setVariableValue(variableDefinitionId, value);
      }
    }
  }

  public void setVariableValue(String variableDefinitionId, Object value) {
    if (variableInstances!=null) {
      VariableInstanceImpl variableInstance = getVariableInstanceLocal(variableDefinitionId);
      if (variableInstance!=null) {
        variableInstance.setValue(value);
        if (updates!=null) {
          updates.isVariableInstancesChanged = true;
          propagateActivityInstanceChange(parent);
        }
      }
    }
    if (parent!=null) {
      parent.setVariableValue(variableDefinitionId, value);
    }
  }
  
  public TypedValue getVariableTypedValue(Object variableDefinitionId) {
    if (variableInstances!=null) {
      VariableInstanceImpl variableInstance = getVariableInstanceLocal(variableDefinitionId);
      if (variableInstance!=null) {
        DataType dataType = variableInstance.variableDefinition.dataType;
        Object value = variableInstance.getValue();
        return new TypedValue(dataType, value);
      }
    }
    if (parent!=null) {
      return parent.getVariableTypedValue(variableDefinitionId);
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
  public ActivityInstanceImpl findActivityInstance(String activityInstanceId) {
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
  public ActivityInstanceImpl findActivityInstanceByActivityDefinitionId(String activityDefinitionId) {
    if (activityDefinitionId==null) {
      return null;
    }
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        ActivityInstanceImpl theOne = activityInstance.findActivityInstanceByActivityDefinitionId(activityDefinitionId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }

  public ServiceRegistry getServiceRegistry() {
    return workflowEngine.getServiceRegistry();
  }

  // updates ////////////////////////////////////////////////////////////

  public boolean hasUpdates() {
    // As long as the workflow instance is not saved, the updates collection is null.
    // That means it's not yet necessary to collect the updates. 
    return updates!=null;
  }
  
  public ScopeInstanceUpdates getUpdates() {
    return updates;
  }
  
  public void trackUpdates(boolean isNew) {
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        activityInstance.trackUpdates(isNew);
      }
    }
    if (variableInstances!=null) {
      for (VariableInstanceImpl variableInstance: variableInstances) {
        variableInstance.trackUpdates(isNew);
      }
    }
  }
  
  protected void propagateActivityInstanceChange(ScopeInstanceImpl scopeInstance) {
    if (scopeInstance!=null) {
      scopeInstance.updates.isActivityInstancesChanged = true;
      propagateActivityInstanceChange(scopeInstance.parent);
    }
  }

  // getters and setters ////////////////////////////////////////////////////////////

  public WorkflowEngineImpl getWorkflowEngine() {
    return workflowEngine;
  }

  public void setWorkflowEngine(WorkflowEngineImpl processEngine) {
    this.workflowEngine = processEngine;
  }

  public WorkflowImpl getWorkflow() {
    return workflow;
  }

  public void setWorkflow(WorkflowImpl processDefinition) {
    this.workflow = processDefinition;
  }
  
  public ScopeImpl getScopeDefinition() {
    return scopeDefinition;
  }

  public void setScopeDefinition(ScopeImpl scopeDefinition) {
    this.scopeDefinition = scopeDefinition;
  }
  
  public WorkflowInstanceImpl getWorkflowInstance() {
    return workflowInstance;
  }
  
  public void setWorkflowInstance(WorkflowInstanceImpl processInstance) {
    this.workflowInstance = processInstance;
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

}

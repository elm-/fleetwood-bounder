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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.definition.ActivityBuilder;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public class ActivityDefinitionImpl extends ScopeDefinitionImpl implements ActivityBuilder {

  @JsonIgnore
  public ActivityType activityType;

  public List<TransitionDefinitionImpl> outgoingTransitionDefinitions;
  public List<ParameterInstanceImpl> parameterInstances;
  public Map<String, ParameterInstanceImpl> parameterInstancesMap;

  // only used during construction
  public String activityTypeId;
  
  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public ActivityDefinitionImpl activityTypeId(String activityTypeId) {
    this.activityTypeId = activityTypeId;
    return this;
  }
  
  public ActivityDefinitionImpl name(String name) {
    super.name(name);
    return this;
  }

  public ActivityDefinitionImpl line(Long line) {
    super.line(line);
    return this;
  }

  public ActivityDefinitionImpl column(Long column) {
    super.column(column);
    return this;
  }
  
  @Override
  public ActivityDefinitionImpl newActivity() {
    return super.newActivity();
  }

  @Override
  public VariableDefinitionImpl newVariable() {
    return super.newVariable();
  }

  @Override
  public TransitionDefinitionImpl newTransition() {
    return super.newTransition();
  }

  @Override
  public TimerDefinitionImpl newTimer() {
    return super.newTimer();
  }

  public ActivityDefinitionImpl parameterValue(ActivityParameter parameter, Object object) {
    return parameterValue(parameter.name, object);
  }

  public ActivityDefinitionImpl parameterValue(String parameterName, Object object) {
    getParameterInstance(parameterName)
      .newParameterBinding()
      .value(object);
    return this;
  }
  
  public ActivityDefinitionImpl parameterExpression(ActivityParameter activityParameter, String expression) {
    return parameterValue(activityParameter.name, expression);
  }
  
  public ActivityDefinitionImpl parameterExpression(String parameterName, String expression) {
    getParameterInstance(parameterName)
      .newParameterBinding()
      .expression(expression);
    return this;
  }
  
  public ActivityDefinitionImpl parameterVariable(ActivityParameter activityParameter, String variableDefinitionRefName) {
    return parameterVariable(activityParameter.name, variableDefinitionRefName);
  }

  public ActivityDefinitionImpl parameterVariable(String parameterName, String variableDefinitionRefName) {
    getParameterInstance(parameterName)
      .newParameterBinding()
      .variable(variableDefinitionRefName);
    return this;
  }

  ParameterInstanceImpl getParameterInstance(String parameterRefName) {
    ParameterInstanceImpl parameterInstance = findParameterInstance(parameterRefName);
    if (parameterInstance==null) {
      parameterInstance = new ParameterInstanceImpl();
      parameterInstance.processEngine = processEngine;
      parameterInstance.processDefinition = processDefinition;
      parameterInstance.parent = this;
      parameterInstance.name = parameterRefName;
      if (parameterInstances==null) {
        parameterInstances = new ArrayList<>();
      }
      parameterInstances.add(parameterInstance);
    }
    return parameterInstance;
  }
  
  public ParameterInstanceImpl findParameterInstance(String parameterId) {
    if (parameterInstancesMap!=null) {
      return parameterInstancesMap.get(parameterId);
    }
    if (parameterInstances!=null) {
      for (ParameterInstanceImpl parameterInstance: parameterInstances) {
        if (parameterId.equals(parameterId)) {
          return parameterInstance;
        }
      }
    }
    return null;
  }

  /// other methods ////////////////////////////

  @Override
  public void prepare() {
    super.prepare();
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

  public ProcessDefinitionPath getPath() {
    return parent.getPath().addActivityDefinitionName(name);
  }

  public boolean isAsync(ActivityInstanceImpl activityInstance) {
    return false;
  }

  public void visit(ProcessDefinitionVisitor visitor, int index) {
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitor.startActivityDefinition(this, index);
    super.visit(visitor);
    visitor.endActivityDefinition(this, index);
  }

  public void addOutgoingTransition(TransitionDefinitionImpl transitionDefinition) {
    if (outgoingTransitionDefinitions==null) {
      outgoingTransitionDefinitions = new ArrayList<TransitionDefinitionImpl>();
    }
    outgoingTransitionDefinitions.add(transitionDefinition);
  }

  public boolean hasOutgoingTransitionDefinitions() {
    return outgoingTransitionDefinitions!=null && !outgoingTransitionDefinitions.isEmpty();
  }

  
  public List<TransitionDefinitionImpl> getOutgoingTransitionDefinitions() {
    return outgoingTransitionDefinitions;
  }

  public void setOutgoingTransitionDefinitions(List<TransitionDefinitionImpl> outgoingTransitionDefinitions) {
    this.outgoingTransitionDefinitions = outgoingTransitionDefinitions;
  }

  public String toString() {
    return name!=null ? "["+name.toString()+"]" : "["+Integer.toString(System.identityHashCode(this))+"]";
  }

}

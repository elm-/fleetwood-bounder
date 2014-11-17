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

import com.heisenberg.api.definition.ActivityBuilder;
import com.heisenberg.impl.ActivityTypeDescriptor;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public class ActivityDefinitionImpl extends ScopeDefinitionImpl implements ActivityBuilder {

  public List<TransitionDefinitionImpl> outgoingTransitionDefinitions;
  public ActivityType activityType;
  public List<ParameterInstanceImpl> parameterInstances;
  public Map<String, ParameterInstanceImpl> parameterInstancesMap;

  // only used during construction
  public String buildActivityTypeRefId;
  
  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public ActivityDefinitionImpl activityType(String activityTypeRefId) {
    this.buildActivityTypeRefId = activityTypeRefId;
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
  
  public ParameterInstanceImpl findParameterInstance(String parameterRefId) {
    if (parameterInstancesMap!=null) {
      return parameterInstancesMap.get(parameterRefId);
    }
    if (parameterInstances!=null) {
      for (ParameterInstanceImpl parameterInstance: parameterInstances) {
        if (parameterRefId.equals(parameterRefId)) {
          return parameterInstance;
        }
      }
    }
    return null;
  }

  public void parse(ParseContext parseContext) {
    if (name==null || "".equals(name)) {
      parseContext.addError(buildLine, buildColumn, "Activity does not have a name");
    }
    ActivityTypeDescriptor activityTypeDescriptor = processEngine.activityTypeDescriptors.get(buildActivityTypeRefId);
    if (activityTypeDescriptor==null) {
      parseContext.addError(buildLine, buildColumn,  
              "Activity %s has invalid type %s.  Must be one of %s", 
              name, 
              buildActivityTypeRefId,
              processEngine.activityTypeDescriptors.keySet());
    } else {
      this.activityType = activityTypeDescriptor.activityType;
    }
    if (parameterInstances!=null) {
      parameterInstancesMap = new HashMap<>();
      for (int i=0; i<parameterInstances.size(); i++) {
        ParameterInstanceImpl parameterInstance = parameterInstances.get(i);
        parseContext.pushPathElement("parameterInstances", parameterInstance.name, i);
        parameterInstance.parse(parseContext);
        parseContext.popPathElement();
        parameterInstancesMap.put(parameterInstance.name, parameterInstance);
      }
    }
    super.parse(parseContext);
  }

  
//  protected void parse(ProcessEngineImpl processEngine, DeployProcessDefinitionResponse response, ProcessDefinitionImpl processDefinition,
//          ScopeDefinitionImpl parent, ActivityBuilder activityDefinition) {
//    this.name = activityDefinition.name;
//    if (activityDefinition.name==null) {
//      response.addError(activityDefinition.location, "Activity has no name");
//    }
//    ActivityTypeDescriptor activityTypeDescriptor = processEngine.activityTypeDescriptors.get(activityDefinition.activityTypeRefId);
//    if (activityTypeDescriptor==null) {
//      response.addError(activityDefinition.location, 
//              "Activity %s has invalid type %s.  Must be one of "+processEngine.activityTypeDescriptors.keySet(), 
//              getActivityErrorReferenceText(activityDefinition), 
//              activityDefinition.activityTypeRefId);
//    } else {
//      this.activityType = activityTypeDescriptor.activityType;
//    }
//    if (activityDefinition.parameterInstances!=null) {
//      for (ParameterInstance parameterInstance: activityDefinition.parameterInstances) {
//        ParameterInstanceImpl parameterInstanceImpl = new ParameterInstanceImpl();
//        parameterInstanceImpl.parse(processEngine, response, processDefinition, this, parameterInstance);
//        if (parameterInstances==null) {
//          parameterInstances = new ArrayList<>();
//          parameterInstancesMap = new HashMap<>();
//        }
//        parameterInstances.add(parameterInstanceImpl);
//        parameterInstancesMap.put(parameterInstanceImpl.name, parameterInstanceImpl);
//      }
//    }
//    super.parse(processEngine, response, processDefinition, parent, activityDefinition);
//  }



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

  @Override
  public void visit(ProcessDefinitionVisitor visitor) {
    visitor.startActivityDefinition(this);
    super.visit(visitor);
    visitor.endActivityDefinition(this);
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

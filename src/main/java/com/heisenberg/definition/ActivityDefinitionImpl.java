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

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.ParameterInstance;
import com.heisenberg.impl.ActivityTypeDescriptor;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public class ActivityDefinitionImpl extends ScopeDefinitionImpl {

  public String name;
  public int index = -1;
  public List<TransitionDefinitionImpl> outgoingTransitionDefinitions;
  public ActivityType activityType;
  public List<ParameterInstanceImpl> parameterInstances;
  public Map<String, ParameterInstanceImpl> parameterInstancesMap;

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

  public ParameterInstanceImpl findParameterInstance(String parameterRefId) {
    return parameterInstancesMap.get(parameterRefId);
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

  protected void parse(ProcessEngineImpl processEngine, DeployProcessDefinitionResponse response, ProcessDefinitionImpl processDefinition,
          ScopeDefinitionImpl parent, ActivityDefinition activityDefinition) {
    this.name = activityDefinition.name;
    if (activityDefinition.name==null) {
      response.addError(activityDefinition.location, "Activity has no name");
    }
    ActivityTypeDescriptor activityTypeDescriptor = processEngine.activityTypeDescriptors.get(activityDefinition.activityTypeRefId);
    if (activityTypeDescriptor==null) {
      response.addError(activityDefinition.location, 
              "Activity %s has invalid type %s.  Must be one of "+processEngine.activityTypeDescriptors.keySet(), 
              getActivityErrorReferenceText(activityDefinition), 
              activityDefinition.activityTypeRefId);
    } else {
      this.activityType = activityTypeDescriptor.activityType;
    }
    if (activityDefinition.parameterInstances!=null) {
      for (ParameterInstance parameterInstance: activityDefinition.parameterInstances) {
        ParameterInstanceImpl parameterInstanceImpl = new ParameterInstanceImpl();
        parameterInstanceImpl.parse(processEngine, response, processDefinition, this, parameterInstance);
        if (parameterInstances==null) {
          parameterInstances = new ArrayList<>();
          parameterInstancesMap = new HashMap<>();
        }
        parameterInstances.add(parameterInstanceImpl);
        parameterInstancesMap.put(parameterInstanceImpl.name, parameterInstanceImpl);
      }
    }
    super.parse(processEngine, response, processDefinition, parent, activityDefinition);
  }
}

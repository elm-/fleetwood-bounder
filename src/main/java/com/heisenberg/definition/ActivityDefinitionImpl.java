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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.impl.SpiDescriptor;
import com.heisenberg.impl.SpiDescriptorField;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.Validator;


/**
 * @author Walter White
 */
public class ActivityDefinitionImpl extends ScopeDefinitionImpl implements ActivityBuilder, ActivityDefinition {

  public String activityTypeId;
  @JsonIgnore
  public Map<String,Object> activityTypeJsonMap;
  public ActivityType activityType;
  public List<TransitionDefinitionImpl> outgoingTransitionDefinitions;

  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public ActivityDefinitionImpl activityType(ActivityType activityType) {
    this.activityType = activityType;
    return this;
  }
  
  public ActivityDefinitionImpl activityTypeJson(Map<String,Object> activityTypeJsonMap) {
    this.activityTypeJsonMap = activityTypeJsonMap;
    return this;
  }
  
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

  /// other methods ////////////////////////////

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

  @Override
  public void initializeBindings(Validator validator) {
    SpiDescriptor activityDescriptor = processEngine.findActivityDescriptor(activityType);
    for (SpiDescriptorField descriptorField: activityDescriptor.getBindingDescriptorFields()) {
      TODO
//        if (Binding.class==field.getType()) {
//          try {
//            Binding<?> b = (Binding<?>) field.get(this);
//            if (b!=null) {
//              b.validate(activityDefinition, this, field, validator);
//            }
//          } catch (Exception e) {
//            throw new RuntimeException(e);
//          }
//        }
    }
  }
}

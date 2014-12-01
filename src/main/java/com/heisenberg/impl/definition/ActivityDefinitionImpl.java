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
package com.heisenberg.impl.definition;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.builder.ActivityBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.ActivityTypeDescriptor;
import com.heisenberg.impl.PluginConfigurationField;
import com.heisenberg.impl.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class ActivityDefinitionImpl extends ScopeDefinitionImpl implements ActivityBuilder, ActivityDefinition {

//  /** References a type declared in the process engine by id.
//   * 
//   * With the {@link ActivityBuilder#activityTypeId() builder}, the activityTypeId can be specified.
//   * The value can be specified directly or indirectly by specifying an activityType object.
//   * When an activityType object is specified, then the {@link ProcessDefinitionValidator validator} and 
//   * {@link ProcessDefinitionSerializer serializer} will initialize the activityTypeId.
//   * 
//   * This value is jsonned and persisted.
//   * This field is mutually exclusive with activityTypeJson. */
//  public String activityTypeId;
  
  /** An inline, jsonnable and persistable declaration of an activityType. 
   * This means that it contains the type and configuration of the activityType.
   * With the {@link ActivityBuilder#activityTypeJson(Map) builder}, an activityType object can be specified.
   * The validator and the serializer. */
  @JsonProperty("activityType") 
  public Map<String,Object> activityTypeJson;
  
  /** The object implementing the activity execution behavior.
   * This object is not persisted nor serialized. 
   * If the process is persisted or serialized, the activityType must be found
   * in the process engine via {@link #activityTypeId} or constructed from {@link #activityTypeJson} */
  @JsonIgnore
  public ActivityType activityType;

  /** the list of transitions leaving this activity.
   * This field is not persisted nor jsonned. It is derived from {@link ScopeDefinitionImpl#transitionDefinitions} */
  @JsonIgnore
  public List<TransitionDefinitionImpl> outgoingTransitionDefinitions;

  /// Activity Definition Builder methods ////////////////////////////////////////////////

  public ActivityDefinitionImpl activityType(ActivityType activityType) {
    this.activityType = activityType;
    return this;
  }
  
  public ActivityDefinitionImpl activityTypeJson(Map<String,Object> activityTypeJsonMap) {
    this.activityTypeJson = activityTypeJsonMap;
    return this;
  }
  
//  public ActivityDefinitionImpl activityTypeId(String activityTypeId) {
//    this.activityTypeId = activityTypeId;
//    return this;
//  }
  
  public ActivityDefinitionImpl id(String id) {
    this.id = id;
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
    return parent.getPath().addActivityDefinitionId(id);
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
    return id!=null ? "["+id.toString()+"]" : "["+Integer.toString(System.identityHashCode(this))+"]";
  }
  
  @Override
  public void initializeBindings(Validator validator) {
    ActivityTypeDescriptor activityDescriptor = processEngine.findActivityDescriptorByClass(activityType.getClass());
    for (PluginConfigurationField descriptorField: activityDescriptor.getBindingConfigurationFields()) {
      Field field = descriptorField.field;
      try {
        Binding<?> binding = (Binding<?>) field.get(activityType);
        if (binding!=null) {
          binding.processEngine = processEngine;
          binding.dataType = descriptorField.dataType;
          binding.validate(this, activityType, descriptorField, validator);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}

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

import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public abstract class ActivityDefinitionImpl extends ScopeDefinitionImpl {

  public ActivityDefinitionId id;
  public List<TransitionDefinitionImpl> outgoingTransitionDefinitions;
  public ActivityType activityDefinition;
  
  public abstract void start(ActivityInstanceImpl activityInstance);

  public ProcessDefinitionPathImpl getPath() {
    Exceptions.checkNotNull(id, "Activity definition doesn't have an id yet");
    Exceptions.checkNotNull(parent, "Activity definition doesn't have an parent yet");
    return parent.getPath().addActivityInstanceId(id);
  }

  public boolean isAsync(ActivityInstanceImpl activityInstance) {
    return false;
  }

  public void signal(ActivityInstanceImpl activityInstance) {
    activityInstance.onwards();
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
    return id!=null ? "["+id.toString()+"]" : "["+Integer.toString(System.identityHashCode(this))+"]";
  }
}

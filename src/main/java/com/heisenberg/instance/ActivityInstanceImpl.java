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

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.api.id.ActivityInstanceId;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.TransitionDefinitionImpl;
import com.heisenberg.engine.operation.ActivityInstanceStartOperation;
import com.heisenberg.engine.operation.NotifyActivityInstanceEndToParent;
import com.heisenberg.engine.updates.ActivityInstanceEndUpdate;
import com.heisenberg.expressions.ScriptRunnerImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.spi.ControllableActivityInstance;


/**
 * @author Walter White
 */
public class ActivityInstanceImpl extends ScopeInstanceImpl implements ActivityInstance, ControllableActivityInstance {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public ActivityInstanceId id;
  
  @JsonIgnore
  public ActivityDefinitionImpl activityDefinition;
  
  public String activityDefinitionName;

  public void onwards() {
    log.debug("Onwards "+this);
    // Default BPMN logic when an activity ends
    // If there are outgoing transitions (in bpmn they are called sequence flows)
    if (activityDefinition.hasOutgoingTransitionDefinitions()) {
      // Ensure that each transition is taken
      // Note that process concurrency does not require java concurrency
      for (TransitionDefinitionImpl transitionDefinition: activityDefinition.getOutgoingTransitionDefinitions()) {
        takeTransition(transitionDefinition);  
      }
    }
    // If non of the transitions is taken
    if (!isEnded()) {
      // Propagate completion upwards
      end(true);
    }
  }

  public void end() {
    end(true);
  }

  void end(boolean notifyParent) {
    if (this.end==null) {
      if (hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are open activity instances: " +this);
      }
      setEnd(Time.now());
      if (notifyParent) {
        processInstance.addOperation(new NotifyActivityInstanceEndToParent(this));
      }
    }
  }

  /** Starts the to (destination) activity in the current (parent) scope.
   * This methods will also end the current activity instance.
   * This method can be called multiple times in one start() */
  public void takeTransition(TransitionDefinition transitionDefinition) {
    ActivityDefinitionImpl to = (ActivityDefinitionImpl) transitionDefinition.getTo();
    end(to!=null);
    if (to!=null) {
      log.debug("Taking transition to "+to);
      ActivityInstanceImpl activityInstance = parent.createActivityInstance(to);
      processInstance.addOperation(new ActivityInstanceStartOperation(activityInstance));
    }
  }
  
  @Override
  public void ended(ActivityInstanceImpl nestedEndedActivityInstance) {
    activityDefinition.activityType.ended(this, nestedEndedActivityInstance);
  }
  
  @Override
  public ActivityInstanceImpl findActivityInstance(ActivityInstanceId activityInstanceId) {
    if (activityInstanceId.equals(this.id)) {
      return this;
    }
    return super.findActivityInstance(activityInstanceId);
  }

  public ActivityInstanceId getId() {
    return id;
  }

  public void setId(ActivityInstanceId id) {
    this.id = id;
  }
  
  public ActivityDefinitionImpl getActivityDefinition() {
    return activityDefinition;
  }
  
  public void setActivityDefinition(ActivityDefinitionImpl activityDefinition) {
    this.activityDefinition = activityDefinition;
  }
  
  public String toString() {
    String activityDefinitionType = activityDefinition.activityType.getClass().getSimpleName();
    return "ai("+activityDefinition.name+"|"+activityDefinitionType+"|"+id+")";
  }
  
  public void setEnd(LocalDateTime end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = new Duration(start.toDateTime(), end.toDateTime()).getMillis();
    }
    processInstance.addUpdate(new ActivityInstanceEndUpdate(this));
  }

  public void visit(ProcessInstanceVisitor visitor, int index) {
    visitor.startActivityInstance(this, index);
    visitCompositeInstance(visitor);
    visitor.endActivityInstance(this, index);
  }

  @Override
  public ScriptRunnerImpl getScriptRunner() {
    return processEngine.scriptRunner;
  }
  
  public void setActivityDefinitionName(String activityDefinitionName) {
    this.activityDefinitionName = activityDefinitionName;
  }

  @Override
  public String getActivityDefinitionName() {
    return activityDefinitionName;
  }
  
  @Override
  public ActivityInstanceImpl findActivityInstanceByName(String activityDefinitionName) {
    if (activityDefinitionName==null) {
      return null;
    }
    if (activityDefinitionName.equals(activityDefinitionName)) {
      return this;
    }
    return super.findActivityInstanceByName(activityDefinitionName);
  }
}

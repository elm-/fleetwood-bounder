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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.ProcessEngine;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.TransitionDefinitionImpl;
import com.heisenberg.engine.operation.NotifyActivityInstanceEndToParent;
import com.heisenberg.engine.updates.ActivityInstanceEndUpdate;
import com.heisenberg.util.Time;


/**
 * @author Walter White
 */
public class ActivityInstanceImpl extends ScopeInstanceImpl {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public ActivityInstanceId id;
  public ActivityDefinitionImpl activityDefinition;
  
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
      if (hasUnfinishedActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are unfinished activity instances: " +this);
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
  public void takeTransition(TransitionDefinitionImpl transitionDefinition) {
    ActivityDefinitionImpl to = transitionDefinition.getTo();
    end(to!=null);
    if (to!=null) {
      log.debug("Taking transition to "+to);
      ActivityInstanceImpl activityInstance = getParent().createActivityInstance(to);
      processInstance.startActivityInstance(activityInstance);
    }
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
    String activityDefinitionIdString = activityDefinition.getId().toString();    
    String activityDefinitionType = activityDefinition.getClass().getSimpleName();
    return "ai("+activityDefinitionIdString+"|"+activityDefinitionType+"|"+id+")";
  }
  
  public void setEnd(Long end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = end-start;
    }
    processInstance.addUpdate(new ActivityInstanceEndUpdate(this));
  }

  @Override
  public Object getJson() {
    return null;
  }
}

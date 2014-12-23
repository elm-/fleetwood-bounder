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

import java.util.HashSet;
import java.util.Set;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.api.definition.Transition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.impl.StartImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ActivityImpl;
import com.heisenberg.impl.definition.TransitionImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.impl.util.Lists;


/**
 * @author Walter White
 */
@JsonPropertyOrder({"id", "activityDefinitionId", "start", "end", "duration", "activityInstances", "variableInstances"})
public class ActivityInstanceImpl extends ScopeInstanceImpl implements ActivityInstance, ControllableActivityInstance {
  
  // WorkflowInstanceImpl.addWork assumes that all starting states start with "start"
  
  public static final String STATE_STARTING = "starting"; 
  public static final String STATE_STARTING_MULTI_CONTAINER = "startingMultiParent"; 
  public static final String STATE_STARTING_MULTI_INSTANCE = "startingMultiInstance"; 
  public static final String STATE_NOTIFYING = "notifying"; 
  public static final String STATE_JOINING = "joining"; 
  public static final String STATE_WAITING = "waiting"; 

  /** @see WorkflowInstanceImpl#isWorkAsync(ActivityInstanceImpl) */
  public static final Set<String> START_WORKSTATES = new HashSet<>(Lists.of(
    STATE_STARTING,
    STATE_STARTING_MULTI_CONTAINER,
    STATE_STARTING_MULTI_INSTANCE));

  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  @JsonIgnore
  public ActivityImpl activityDefinition;
  
  public String activityDefinitionId;
  public String workState;
  public String calledWorkflowInstanceId;

  public void onwards() {
    log.debug("Onwards "+this);
    // Default BPMN logic when an activity ends
    // If there are outgoing transitions (in bpmn they are called sequence flows)
    if (activityDefinition.hasOutgoingTransitionDefinitions()) {
      // Ensure that each transition is taken
      // Note that process concurrency does not require java concurrency
      for (TransitionImpl transitionDefinition: activityDefinition.outgoingDefinitions) {
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

  public void end(boolean notifyParent) {
    if (this.end==null) {
      if (hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are open activity instances: " +this);
      }
      setEnd(Time.now());
      if (notifyParent) {
        setWorkState(STATE_NOTIFYING);
        workflowInstance.addWork(this);

      } else {
        setWorkState(null); // means please archive me.
      }
    }
  }

  public void setWorkState(String workState) {
    this.workState = workState;
    if (updates!=null) {
      getUpdates().isWorkStateChanged = true;
      propagateActivityInstanceChange(parent);
    }
  }

  @Override
  public void setJoining() {
    setWorkState(STATE_JOINING);
  }
  
  @Override
  public boolean isJoining(ActivityInstance activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    return STATE_JOINING.equals(activityInstanceImpl.workState);
  }
  
  @Override
  public void removeJoining(ActivityInstance activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    activityInstanceImpl.setWorkState(null);
  }

  /** Starts the to (destination) activity in the current (parent) scope.
   * This methods will also end the current activity instance.
   * This method can be called multiple times in one start() */
  public void takeTransition(Transition transition) {
    ActivityImpl to = (ActivityImpl) transition.getTo();
    end(to==null);
    if (to!=null) {
      log.debug("Taking transition to "+to);
      parent.createActivityInstance(to);
    }
  }
  
  @Override
  public void ended(ActivityInstanceImpl nestedEndedActivityInstance) {
    activityDefinition.activityType.ended(this, nestedEndedActivityInstance);
  }
  
  @Override
  public ActivityInstanceImpl findActivityInstance(String activityInstanceId) {
    if (activityInstanceId.equals(this.id)) {
      return this;
    }
    return super.findActivityInstance(activityInstanceId);
  }

  public ActivityImpl getActivity() {
    return activityDefinition;
  }
  
  public void setActivityDefinition(ActivityImpl activityDefinition) {
    this.activityDefinition = activityDefinition;
  }
  
  public String toString() {
    String activityDefinitionType = activityDefinition.activityType.getClass().getSimpleName();
    return "("+activityDefinition.id+"|"+activityDefinitionType+"|"+id+"|ai)";
  }
  
  public void setEnd(LocalDateTime end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = new Duration(start.toDateTime(), end.toDateTime()).getMillis();
    }
    if (updates!=null) {
      updates.isEndChanged = true;
      propagateActivityInstanceChange(parent);
    }
  }

  public void setActivityDefinitionId(String activityDefinitionId) {
    this.activityDefinitionId = activityDefinitionId;
  }

  @Override
  public String getActivityId() {
    return activityDefinitionId;
  }
  
  @Override
  public ActivityInstanceImpl findActivityInstanceByActivityDefinitionId(String activityDefinitionId) {
    if (activityDefinitionId==null) {
      return null;
    }
    if (activityDefinitionId.equals(this.activityDefinitionId)) {
      return this;
    }
    return super.findActivityInstanceByActivityDefinitionId(activityDefinitionId);
  }
  
  public Object getTransientContextObject(String key) {
    return workflowInstance.getTransientContextObject(key);
  }

  @Override
  public boolean isProcessInstance() {
    return false;
  }

  public StartBuilder newSubWorkflowStart(String subSubWorkflowId) {
    JsonService jsonService = workflowEngine.getServiceRegistry().getService(JsonService.class);
    StartImpl start = new StartImpl(workflowEngine, jsonService);
    start.processDefinitionId = subSubWorkflowId;
    start.callerWorkflowInstanceId = workflowInstance.id;
    start.callerActivityInstanceId = id;
    return start;
  }

  public void setCalledWorkflowInstanceId(String calledWorkflowInstanceId) {
    this.calledWorkflowInstanceId = calledWorkflowInstanceId;
  }
  
  public String getCalledWorkflowInstanceId() {
    return calledWorkflowInstanceId;
  }

  @Override
  public ActivityInstanceUpdates getUpdates() {
    return (ActivityInstanceUpdates) updates;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return workflowEngine.getServiceRegistry();
  }
  
  public void trackUpdates(boolean isNew) {
    if (updates==null) {
      updates = new ActivityInstanceUpdates(isNew);
    } else {
      updates.reset(isNew);
    }
    super.trackUpdates(isNew);
  }
}

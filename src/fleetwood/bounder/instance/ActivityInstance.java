/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.engine.ProcessEngineImpl;
import fleetwood.bounder.engine.updates.ActivityInstanceEnd;
import fleetwood.bounder.engine.updates.StateUpdate;
import fleetwood.bounder.util.Time;


/**
 * @author Walter White
 */
public class ActivityInstance extends CompositeInstance {
  
  protected ActivityInstanceId id;
  protected ActivityInstanceState state;
  
  @JsonIgnore
  protected ActivityDefinition activityDefinition;
  
  public void onwards() {
    ProcessEngineImpl.log.debug("Ended "+this);
    end();
    if (activityDefinition.hasTransitionDefinitions()) {
      for (TransitionDefinition transitionDefinition: activityDefinition.getTransitionDefinitions()) {
        takeTransition(transitionDefinition);  
      }
    }
  }

  public void end() {
    if (this.end==null) {
      this.end = Time.now();
      processInstance.addUpdate(new ActivityInstanceEnd(processEngine, this));
    }
  }

  public void takeTransition(TransitionDefinition transitionDefinition) {
    ActivityDefinition to = transitionDefinition.getTo();
    if (to!=null) {
      ActivityInstance activityInstance = getParent().createActivityInstance(to);
      processInstance.startActivityInstance(activityInstance);
    }
  }
  
  @Override
  public ActivityInstance findActivityInstance(ActivityInstanceId activityInstanceId) {
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
  
  public ActivityDefinition getActivityDefinition() {
    return activityDefinition;
  }
  
  public void setActivityDefinition(ActivityDefinition activityDefinition) {
    this.activityDefinition = activityDefinition;
  }
  
  public String toString() {
    return "["+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+"|"+activityDefinition.getClass().getSimpleName()+"]";
  }

  
  public ActivityInstanceState getState() {
    return state;
  }
  
  public void setState(ActivityInstanceState state) {
    this.state = state;
    processInstance.addUpdate(new StateUpdate(processEngine, this, state));
  }

  public void setStateCreated() {
    setState(ActivityInstanceState.CREATED);
  }

  public void setStateAsync() {
    setState(ActivityInstanceState.ASYNC);
  }

  public void setStateStarting() {
    setState(ActivityInstanceState.STARTING);
  }

  public void setStateWaiting() {
    setState(ActivityInstanceState.WAITING);
  }
}

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
import fleetwood.bounder.engine.updates.ActivityInstanceEndUpdate;
import fleetwood.bounder.util.Time;


/**
 * @author Walter White
 */
public class ActivityInstance extends CompositeInstance {
  
  protected ActivityInstanceId id;
  
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
      if (hasUnfinishedActivityInstances()) {
        throw new RuntimeException("Can't end this activity instance. There are unfinished activity instances. "+processEngine.getJson().toJsonStringPretty(processInstance));
      }
      this.end = Time.now();
      processInstance.addUpdate(new ActivityInstanceEndUpdate(this));
    }
  }

  public boolean hasUnfinishedActivityInstances() {
    if (activityInstances==null) {
      return false;
    }
    for (ActivityInstance activityInstance: activityInstances) {
      if (!activityInstance.isEnded()) {
        return true;
      }
    }
    return false;
  }

  /** Starts the to (destination) activity in the current (parent) scope.
   * This methods will also end the current activity instance.
   * This method can be called multiple times in one start() */
  public void takeTransition(TransitionDefinition transitionDefinition) {
    end();
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
    return "ai("+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+"|"+activityDefinition.getClass().getSimpleName()+")";
  }
}

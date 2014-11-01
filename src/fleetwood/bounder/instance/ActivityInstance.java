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

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.engine.StartActivityInstance;
import fleetwood.bounder.util.Time;


/**
 * @author Tom Baeyens
 */
public class ActivityInstance extends CompositeInstance {
  
  protected ActivityInstanceId id;
  protected ActivityDefinition activityDefinition;
  protected Long start;
  protected Long end;
  
  public void signal() {
  }

  public void onwards() {
    ProcessEngine.log.debug("Ended "+this);
    end();
    if (activityDefinition.hasTransitionDefinitions()) {
      for (TransitionDefinition transitionDefinition: activityDefinition.getTransitionDefinitions().values()) {
        takeTransition(transitionDefinition);  
      }
    }
  }

  public void end() {
    if (this.end==null) {
      this.end = Time.now();
    }
  }

  public void takeTransition(TransitionDefinition transitionDefinition) {
    ActivityDefinition to = transitionDefinition.getTo();
    if (to!=null) {
      ActivityInstance activityInstance = getParent().createActivityInstance(to);
      processInstance.addOperation(new StartActivityInstance(activityInstance));
    }
  }
  
  @Override
  public ActivityInstance findActivityInstance(ActivityInstanceId activityInstanceId) {
    if (activityInstanceId.equals(this.id)) {
      return this;
    }
    return super.findActivityInstance(activityInstanceId);
  }

  public boolean isEnded() {
    return end!=null;
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
  
  public Long getStart() {
    return start;
  }
  
  public void setStart(Long start) {
    this.start = start;
  }
  
  public Long getEnd() {
    return end;
  }
  
  public void setEnd(Long end) {
    this.end = end;
  }
  
  public String toString() {
    return "["+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+"|"+activityDefinition.getClass().getSimpleName()+"]";
  }
}

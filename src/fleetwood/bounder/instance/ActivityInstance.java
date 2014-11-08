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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.engine.operation.NotifyActivityInstanceEndToParent;
import fleetwood.bounder.engine.updates.ActivityInstanceEndUpdate;
import fleetwood.bounder.json.Serializer;
import fleetwood.bounder.util.Time;


/**
 * @author Walter White
 */
public class ActivityInstance extends CompositeInstance {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public static final String FIELD_ID = "id";
  protected ActivityInstanceId id;
  
  public static final String FIELD_ACTIVITY_DEFINITION_ID = "activityDefinitionId";
  protected ActivityDefinition activityDefinition;
  
  public void onwards() {
    log.debug("Onwards "+this);
    // Default BPMN logic when an activity ends
    // If there are outgoing transitions (in bpmn they are called sequence flows)
    if (activityDefinition.hasOutgoingTransitionDefinitions()) {
      // Ensure that each transition is taken
      // Note that process concurrency does not require java concurrency
      for (TransitionDefinition transitionDefinition: activityDefinition.getOutgoingTransitionDefinitions()) {
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
  public void takeTransition(TransitionDefinition transitionDefinition) {
    ActivityDefinition to = transitionDefinition.getTo();
    end(to!=null);
    if (to!=null) {
      log.debug("Taking transition to "+to);
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
  public void serialize(Serializer serializer) {
    serializer.objectStart(this);
    serializer.writeIdField(FIELD_ID, id);
    serializer.writeIdField(FIELD_ACTIVITY_DEFINITION_ID, activityDefinition!=null ? activityDefinition.getId() : null);
    serializeCompositeInstanceFields(serializer);
    serializer.objectEnd(this);
  }
}

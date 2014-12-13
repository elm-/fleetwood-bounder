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
package com.heisenberg.api.activities.bpmn;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
@JsonTypeName("parallelGateway")
public class ParallelGateway extends AbstractActivityType {
  
  @JsonIgnore
  int nbrOfIncomingTransitions = -1;
  @JsonIgnore
  boolean hasOutgoingTransitions = false;
  
  
  @Override
  public void validate(ActivityDefinition activity, Validator validator) {
    log.debug("validating "+activity.getId());
    
    // at least one in, at least one out
    List<TransitionDefinition> incomingTransitions = activity.getIncomingTransitionDefinitions();
    log.debug("  incoming "+incomingTransitions.size());
    if (incomingTransitions==null || incomingTransitions.isEmpty()) {
      validator.addWarning("Parallel gateway '%s' does not have incoming transitions", activity.getId());
    } else {
      nbrOfIncomingTransitions = incomingTransitions.size();
    }
    List<TransitionDefinition> outgoingTransitions = activity.getOutgoingTransitionDefinitions();
    log.debug("  outgoing "+outgoingTransitions.size());
    if (outgoingTransitions==null || outgoingTransitions.isEmpty()) {
      validator.addWarning("Parallel gateway '%s' does not have outgoing transitions", activity.getId());
    } else {
      hasOutgoingTransitions = true;
    }
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    activityInstance.end();
    boolean hasOtherUnfinishedActivities = false;

    List<ActivityInstance> otherJoiningActivityInstances = new ArrayList<>();
    for (ActivityInstance siblingActivityInstance: activityInstance.getParent().getActivityInstances()) {
      if (!siblingActivityInstance.isEnded()) {
        hasOtherUnfinishedActivities = true;
      }
      if ( siblingActivityInstance!=activityInstance
           && siblingActivityInstance.getActivityDefinition()==activityInstance.getActivityDefinition()
           && siblingActivityInstance.isJoining() ) {
        otherJoiningActivityInstances.add(siblingActivityInstance);
      }
    }
    
    if ( hasOutgoingTransitions
         && ( otherJoiningActivityInstances.size()==(nbrOfIncomingTransitions-1)
              || !hasOtherUnfinishedActivities
            )
       ) {
      log.debug("firing parallel gateway");
      for (ActivityInstance otherJoiningActivityInstance: otherJoiningActivityInstances) {
        otherJoiningActivityInstance.removeJoining();
      }
      activityInstance.onwards();
    } else {
      ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
      activityInstanceImpl.setJoining();
    }
  }

  @Override
  public void message(ControllableActivityInstance activityInstance) {
  }

  @Override
  public void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance) {
  }
}

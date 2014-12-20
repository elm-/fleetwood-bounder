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
package com.heisenberg.api.activitytypes;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.api.definition.Transition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.impl.plugin.AbstractActivityType;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Validator;


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
  public void validate(Activity activity, Validator validator) {
    log.debug("Validating "+activity.getId());
    
    // at least one in, at least one out
    List<Transition> incomingTransitions = activity.getIncomingTransitions();
    log.debug("  incoming "+incomingTransitions.size());
    if (incomingTransitions==null || incomingTransitions.isEmpty()) {
      validator.addWarning("Parallel gateway '%s' does not have incoming transitions", activity.getId());
    } else {
      nbrOfIncomingTransitions = incomingTransitions.size();
    }
    List<Transition> outgoingTransitions = activity.getOutgoingTransitions();
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
           && siblingActivityInstance.getActivity()==activityInstance.getActivity()
           && activityInstance.isJoining(siblingActivityInstance) ) {
        otherJoiningActivityInstances.add(siblingActivityInstance);
      }
    }
    
    if ( hasOutgoingTransitions
         && ( otherJoiningActivityInstances.size()==(nbrOfIncomingTransitions-1)
              || !hasOtherUnfinishedActivities
            )
       ) {
      log.debug("Firing parallel gateway");
      for (ActivityInstance otherJoiningActivityInstance: otherJoiningActivityInstances) {
        activityInstance.removeJoining(otherJoiningActivityInstance);
      }
      activityInstance.onwards();
    } else {
      activityInstance.setJoining();
    }
  }

  @Override
  public void message(ControllableActivityInstance activityInstance) {
  }

  @Override
  public void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance) {
  }
}

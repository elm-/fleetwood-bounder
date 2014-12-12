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

import java.util.List;
import java.util.Map;

import javax.script.CompiledScript;

import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.configuration.Script;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.script.ScriptResult;


/**
 * @author Walter White
 */
public class ExclusiveGateway extends AbstractActivityType {

  CompiledScript transitionIdExpression;
  Map<String,CompiledScript> transitionExpressions;
  
  @Override
  public void start(ControllableActivityInstance activityInstance) {
    ActivityDefinition activityDefinition = activityInstance.getActivityDefinition();
    List<TransitionDefinition> outgoingTransitions = activityDefinition.getOutgoingTransitionDefinitions();
    TransitionDefinition defaultTransition = activityDefinition.getDefaultTransition();
    // if there are less than two edges, ignore the conditions
    if (outgoingTransitions != null && outgoingTransitions.size() > 1) {  
      TransitionDefinition transition = findFirstTransitionThatMeetsCondition(activityInstance, outgoingTransitions);
      if (transition != null) {
        activityInstance.takeTransition(transition);
      } else if (defaultTransition != null) {
        activityInstance.takeTransition(defaultTransition);
      } else {
        activityInstance.end(false);
      }
      return;
    }

    // no outgoing transitions. just end here and notify the parent this execution path ended.
    activityInstance.end();
  }

  protected TransitionDefinition findFirstTransitionThatMeetsCondition(ControllableActivityInstance activityInstance, List<TransitionDefinition> outgoingTransitions) {
    if (outgoingTransitions != null) {
      for (TransitionDefinition outgoingTransition: outgoingTransitions ) {
        // condition must be true and the transition must have a target
        if (meetsCondition(outgoingTransition, activityInstance)) {
          return outgoingTransition;
        }
      }
    }
    return null;
  }

  protected boolean meetsCondition(TransitionDefinition outgoingTransition, ControllableActivityInstance activityInstance) {
    Script script = outgoingTransition.getConditionScript();
    if (script!=null) {
      ScriptService scriptService = activityInstance.getServiceLocator().getScriptService();
      ScriptResult scriptResult = evaluateCondition(activityInstance, outgoingTransition, script, scriptService);
      if (Boolean.TRUE.equals(scriptResult.getResult())) {
        return true;
      }
    }
    return false;
  }

  protected ScriptResult evaluateCondition(ControllableActivityInstance activityInstance, TransitionDefinition outgoingTransition, Script script, ScriptService scriptService) {
    return scriptService.evaluateScript(activityInstance, script);
  }
}

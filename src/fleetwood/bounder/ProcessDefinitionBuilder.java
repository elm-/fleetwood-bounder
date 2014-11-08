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

package fleetwood.bounder;

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.definition.VariableDefinition;


/**
 * @author Walter White
 */
public class ProcessDefinitionBuilder {
  
  protected ProcessDefinition processDefinition = new ProcessDefinition();

  public ProcessDefinitionBuilder activity(ActivityDefinition activityDefinition) {
    processDefinition.addActivityDefinition(activityDefinition);
    return this;
  }

  public ProcessDefinitionBuilder transition(ActivityDefinition from, ActivityDefinition to) {
    processDefinition.createTransitionDefinition(from, to);
    return this;
  }

  public ProcessDefinitionBuilder transition(TransitionDefinition transitionDefinition) {
    processDefinition.addTransitionDefinition(transitionDefinition);
    return this;
  }

  public ProcessDefinitionBuilder variable(VariableDefinition<?> variableDefinition) {
    processDefinition.addVariableDefinition(variableDefinition);
    return this;
  }

  public ProcessDefinition get() {
    return processDefinition;
  }
}

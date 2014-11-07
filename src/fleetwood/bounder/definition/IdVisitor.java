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

package fleetwood.bounder.definition;

import java.util.UUID;

import fleetwood.bounder.instance.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class IdVisitor extends ProcessDefinitionVisitor {

  protected ProcessEngineImpl processEngine;
  protected int activityDefinitionsCreated = -1;   // first id used is 0
  protected int transitionDefinitionsCreated = -1; // first id used is 0
  protected int variableDefinitionsCreated = -1;   // first id used is 0
  
  public IdVisitor(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public void startProcessDefinition(ProcessDefinition processDefinition) {
    if (processDefinition.getId()==null) {
      processDefinition.setId(createProcessDefinitionId(processDefinition));
    }
  }
  
  @Override
  public void startActivityDefinition(ActivityDefinition activityDefinition) {
    if (activityDefinition.getId()==null) {
      activityDefinition.setId(createActivityDefinitionId(activityDefinition));
    }
  }

  @Override
  public void variableDefinition(VariableDefinition variableDefinition) {
    if (variableDefinition.getId()==null) {
      variableDefinition.setId(createVariableDefinitionId(variableDefinition));
    }
  }

  @Override
  public void transitionDefinition(TransitionDefinition transitionDefinition) {
    if (transitionDefinition.getId()==null) {
      transitionDefinition.setId(createTransitionDefinitionId(transitionDefinition));
    }
  }

  public ProcessDefinitionId createProcessDefinitionId(ProcessDefinition processDefinition) {
    return new ProcessDefinitionId(UUID.randomUUID());
  }

  public ActivityDefinitionId createActivityDefinitionId(ActivityDefinition activityDefinition) {
    activityDefinitionsCreated++;
    return new ActivityDefinitionId(activityDefinitionsCreated);
  }

  public VariableDefinitionId createVariableDefinitionId(VariableDefinition variableDefinition) {
    variableDefinitionsCreated++;
    return new VariableDefinitionId(variableDefinitionsCreated);
  }

  public TransitionDefinitionId createTransitionDefinitionId(TransitionDefinition transitionDefinition) {
    transitionDefinitionsCreated++;
    return new TransitionDefinitionId(transitionDefinitionsCreated);
  }
}

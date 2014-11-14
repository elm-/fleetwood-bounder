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
package com.heisenberg.definition;

import java.util.UUID;

import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class EnsureIdVisitor extends ProcessDefinitionVisitor {

  protected ProcessEngineImpl processEngine;
  protected int activityDefinitionsCreated = -1;   // first id used is 0
  protected int transitionDefinitionsCreated = -1; // first id used is 0
  protected int variableDefinitionsCreated = -1;   // first id used is 0
  
  public EnsureIdVisitor(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
    if (processDefinition.getId()==null) {
      processDefinition.setId(createProcessDefinitionId(processDefinition));
    }
  }
  
  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activityDefinition) {
    if (activityDefinition.getId()==null) {
      activityDefinition.id(createActivityDefinitionId(activityDefinition));
    }
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variableDefinition) {
    if (variableDefinition.getId()==null) {
      variableDefinition.setId(createVariableDefinitionId(variableDefinition));
    }
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transitionDefinition) {
    if (transitionDefinition.getId()==null) {
      transitionDefinition.setId(createTransitionDefinitionId(transitionDefinition));
    }
  }

  public ProcessDefinitionId createProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return new ProcessDefinitionId(UUID.randomUUID());
  }

  public ActivityDefinitionId createActivityDefinitionId(ActivityDefinitionImpl activityDefinition) {
    activityDefinitionsCreated++;
    return new ActivityDefinitionId("a"+activityDefinitionsCreated);
  }

  public VariableDefinitionId createVariableDefinitionId(VariableDefinitionImpl variableDefinition) {
    variableDefinitionsCreated++;
    return new VariableDefinitionId("v"+variableDefinitionsCreated);
  }

  public TransitionDefinitionId createTransitionDefinitionId(TransitionDefinitionImpl transitionDefinition) {
    transitionDefinitionsCreated++;
    return new TransitionDefinitionId("t"+transitionDefinitionsCreated);
  }
}

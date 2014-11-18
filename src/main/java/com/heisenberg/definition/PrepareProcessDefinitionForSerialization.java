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



/** 
 * @author Walter White
 */
public class PrepareProcessDefinitionForSerialization extends ProcessDefinitionVisitor {

  @Override
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }

  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activityDefinition) {
    if (activityDefinition.activityTypeId==null && activityDefinition.activityType!=null) {
      activityDefinition.activityTypeId = activityDefinition.activityType.getId();
    }
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variableDefinition) {
    if (variableDefinition.typeId==null && variableDefinition.type!=null) {
      variableDefinition.typeId = variableDefinition.type.getId();
    }
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transitionDefinition) {
    if (transitionDefinition.fromName==null && transitionDefinition.from!=null) {
      transitionDefinition.fromName = transitionDefinition.from.name;
    }
    if (transitionDefinition.toName==null && transitionDefinition.to!=null) {
      transitionDefinition.toName = transitionDefinition.to.name;
    }
  }
}

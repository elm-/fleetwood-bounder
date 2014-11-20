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

import com.heisenberg.spi.Type;



/** 
 * @author Walter White
 */
public class PrepareProcessDefinitionForSerialization implements ProcessDefinitionVisitor {

  @Override
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }

  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activityDefinition, int index) {
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variableDefinition, int index) {
    if (variableDefinition.typeId==null && variableDefinition.type!=null) {
      variableDefinition.typeId = variableDefinition.type.getId();
    }
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transitionDefinition, int index) {
    if (transitionDefinition.fromName==null && transitionDefinition.from!=null) {
      transitionDefinition.fromName = transitionDefinition.from.name;
    }
    if (transitionDefinition.toName==null && transitionDefinition.to!=null) {
      transitionDefinition.toName = transitionDefinition.to.name;
    }
  }

  @Override
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }

  @Override
  public void endActivityDefinition(ActivityDefinitionImpl activityDefinition, int index) {
  }

  @Override
  public void type(Type type, int index) {
  }
}

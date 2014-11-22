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
package com.heisenberg.impl.definition;

import com.heisenberg.api.type.DataType;


/**
 * @author Walter White
 */
public interface ProcessDefinitionVisitor {

  /** invoked only for process definitions */
  void startProcessDefinition(ProcessDefinitionImpl processDefinition);

  /** invoked only for process definitions */
  void endProcessDefinition(ProcessDefinitionImpl processDefinition);

  /** invoked for types inside a process definitions.
   * This will be invoked after the startProcessDefinition and before any of the other elements inside the process definition. */
  void dataType(DataType dataType, int index);

  /** invoked only for process definitions and activity definitions */
  void startActivityDefinition(ActivityDefinitionImpl activityDefinition, int index);

  /** invoked only for process definitions and activity definitions */
  void endActivityDefinition(ActivityDefinitionImpl activityDefinition, int index);

  /** visit variable definitions */
  void variableDefinition(VariableDefinitionImpl variableDefinition, int index);

  /** visit transition definitions */
  void transitionDefinition(TransitionDefinitionImpl transitionDefinition, int index);

}

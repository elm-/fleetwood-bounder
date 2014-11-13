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
public class UnresolvedTransitionDefinition {

  public TransitionDefinition transitionDefinition;
  public ActivityDefinitionId fromId;
  public ActivityDefinitionId toId;
  
  public void resolve(ScopeDefinition scopeDefinition) {
    ActivityDefinition from = scopeDefinition.getActivityDefinition(fromId);
    ActivityDefinition to = scopeDefinition.getActivityDefinition(toId);
    transitionDefinition.setFrom(from);
    transitionDefinition.setTo(to);
  }
}

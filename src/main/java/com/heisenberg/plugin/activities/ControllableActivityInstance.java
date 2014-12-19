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
package com.heisenberg.plugin.activities;

import java.util.List;

import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.plugin.ServiceRegistry;
import com.heisenberg.plugin.TypedValue;


/**
 * @author Walter White
 */
public interface ControllableActivityInstance extends ActivityInstance {

  Object getVariableValue(String variableDefinitionId);
  void setVariableValue(String callerVariableId, Object value);

  TypedValue getVariableTypedValue(Object variableDefinitionId);
  
  <T> T getValue(Binding<T> binding);
  <T> List<T> getValue(List<Binding<T>> bindings);
  
  Object getTransientContextObject(String key);

  /** ends this activity instance, takes outgoing transitions if there are any and if not, notifies the parent this execution path has ended. */
  void onwards();

  /** ends this activity instance and notifies the parent that this execution path has ended. */
  void end();

  /** ends this activity instance and optionally notifies the parent that this execution path has ended. */
  void end(boolean notifyParent);

  /** starts a nested activity instance for the given activity definition */
  void start(ActivityDefinition activityDefinition);

  void takeTransition(TransitionDefinition transitionDefinition);
  
  ServiceRegistry getServiceRegistry();

}

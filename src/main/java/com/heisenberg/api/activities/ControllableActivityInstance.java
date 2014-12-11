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
package com.heisenberg.api.activities;

import java.util.List;

import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.definition.TransitionDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.util.ServiceLocator;
import com.heisenberg.api.util.TypedValue;


/**
 * @author Walter White
 */
public interface ControllableActivityInstance extends ActivityInstance {

  TypedValue getVariableValueRecursive(Object variableDefinitionId);
  <T> T getValue(Binding<T> binding);
  <T> List<T> getValueList(List<Binding<T>> bindings);
  
  Object getTransientContextObject(String key);

  void onwards();

  void end();

  /** starts a nested activity instance for the given activity definition */
  void start(ActivityDefinition activityDefinition);

  void takeTransition(TransitionDefinition transitionDefinition);
  
  ServiceLocator getServiceLocator();
  void setJoining();
}

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

import java.lang.reflect.Field;

import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ScopeInstance;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.SpiDescriptor;
import com.heisenberg.spi.ControllableActivityInstance;
import com.heisenberg.spi.Validator;
import com.heisenberg.util.Reflection;




/**
 * @author Walter White
 */
public abstract class AbstractActivityType implements ActivityType {
  
  public abstract void start(ControllableActivityInstance activityInstance);

  public void signal(ControllableActivityInstance activityInstance) {
    activityInstance.onwards();
  }
  
  public void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance) {
    ScopeInstance parentScopeInstance = activityInstance.getParent();
    if (!parentScopeInstance.hasOpenActivityInstances()) {
      parentScopeInstance.end();
    }
  }

  @Override
  public void validate(ActivityDefinition activityDefinition, Validator validator) {
    // activityDefinition.initializeBindings(validator);
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getLabel() {
    return null;
  }
}
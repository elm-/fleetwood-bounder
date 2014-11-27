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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.util.Plugin;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.json.ActivityTypeIdResolver;



/**
 * @author Walter White
 */
@JsonTypeInfo(use=Id.CUSTOM, include=As.PROPERTY, property="typeId")
@JsonTypeIdResolver(ActivityTypeIdResolver.class)
public interface ActivityType extends Plugin {

  /** called when the process is being deployed. 
   * @param activity */
  void validate(ActivityDefinition activity, Validator validator);

  /** called when the activity instance is started */
  void start(ControllableActivityInstance activityInstance);

  /** called when an external signal is invoked on this activity instance through the process engine api */
  void notify(ControllableActivityInstance activityInstance);

  /** called when a nested activity instance is ended */
  void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance);
}

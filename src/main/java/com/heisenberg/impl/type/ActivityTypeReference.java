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
package com.heisenberg.impl.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.plugin.Validator;
import com.heisenberg.plugin.activities.ActivityType;
import com.heisenberg.plugin.activities.ControllableActivityInstance;


/**
 * @author Walter White
 */
@JsonTypeName("activityTypeReference")
public class ActivityTypeReference implements ActivityType {

  public String typeId;
  
  @JsonIgnore
  public ActivityType delegate;
  
  public ActivityTypeReference() {
  }

  public ActivityTypeReference(String typeId) {
    this.typeId = typeId;
  }

  @Override
  public void validate(Activity activity, Validator validator) {
    delegate.validate(activity, validator);
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    delegate.start(activityInstance);
  }

  @Override
  public void message(ControllableActivityInstance activityInstance) {
    delegate.message(activityInstance);
  }

  @Override
  public void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance) {
    delegate.ended(activityInstance, nestedEndedActivityInstance);
  }

}

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
package com.heisenberg.api.activities.bpmn;

import java.util.List;

import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
public class EmbeddedSubprocess extends AbstractActivityType {

  public static final EmbeddedSubprocess INSTANCE = new EmbeddedSubprocess();
  
  @Override
  public String getTypeId() {
    return "embeddedSubprocess";
  }
  
  @Override
  public void validate(ActivityDefinition activityDefinition, Validator validator) {
    activityDefinition.initializeStartActivities(validator);
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    List<ActivityDefinition> startActivities = activityInstance.getActivityDefinition().getStartActivities();
    if (startActivities!=null && !startActivities.isEmpty()) {
      for (ActivityDefinition startActivity: startActivities) {
        activityInstance.start(startActivity);
      }
    } else {
      activityInstance.onwards();
    }
  }
}

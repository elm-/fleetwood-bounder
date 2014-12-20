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
package com.heisenberg.api.activitytypes;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.impl.plugin.AbstractActivityType;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Validator;


/**
 * @author Walter White
 */
@JsonTypeName("embeddedSubprocess")
public class EmbeddedSubprocess extends AbstractActivityType {

  public static final EmbeddedSubprocess INSTANCE = new EmbeddedSubprocess();
  
  @Override
  public void validate(Activity activity, Validator validator) {
    activity.initializeStartActivities(validator);
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    List<Activity> startActivities = activityInstance.getActivity().getStartActivities();
    if (startActivities!=null && !startActivities.isEmpty()) {
      for (Activity startActivity: startActivities) {
        activityInstance.start(startActivity);
      }
    } else {
      activityInstance.onwards();
    }
  }
}

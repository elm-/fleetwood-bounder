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
package com.heisenberg.impl;

import java.util.HashMap;
import java.util.Map;

import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.ActivityType;


/**
 * @author Walter White
 */
public class ActivityTypeDescriptor {

  public String activityTypeId;
  public Map<String, ActivityParameter> activityParameters;
  public Class<? extends ActivityType> activityTypeClass;
  public ActivityType activityType;
  
  public static ActivityTypeDescriptor typeId(String activityTypeId) {
    ActivityTypeDescriptor activityDescriptor = new ActivityTypeDescriptor();
    activityDescriptor.activityTypeId = activityTypeId;
    return activityDescriptor;
  }

  public ActivityTypeDescriptor parameter(ActivityParameter activityParameter) {
    if (activityParameters==null) {
      activityParameters = new HashMap<>();
    }
    activityParameters.put(activityParameter.name, activityParameter);
    return this;
  }
}

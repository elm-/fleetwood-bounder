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

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.activities.ActivityType;


/**
 * @author Walter White
 */
public class ActivityTypeDescriptor extends PluginDescriptor {

  protected ActivityType activityType = null;
  protected List<PluginConfigurationField> bindingConfigurationFields;

  public ActivityTypeDescriptor(ProcessEngineImpl processEngine, ActivityType activityType) {
    super(processEngine, activityType);
    if (configurationFields==null) {
      this.activityType = activityType;
    } else {
      for (PluginConfigurationField field: configurationFields) {
        if (field.dataType.getClass()==BindingType.class) {
          if (bindingConfigurationFields==null) {
            bindingConfigurationFields = new ArrayList<>();
          }
          bindingConfigurationFields.add(field);
        }
      }
    }
  }
  
  public ActivityType getActivityType() {
    return activityType;
  }

  public List<PluginConfigurationField> getBindingConfigurationFields() {
    return bindingConfigurationFields;
  }
}

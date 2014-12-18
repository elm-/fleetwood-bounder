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
package com.heisenberg.impl.plugin;

import static com.heisenberg.impl.plugin.PluginHelper.getJsonTypeName;

import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.plugin.activities.ActivityType;


/**
 * @author Walter White
 */
public abstract class ActivityTypeRegistration {
  
  public abstract void register(ProcessEngineImpl processEngine, ActivityTypeService activityTypeService);

  public static class Singleton extends ActivityTypeRegistration {
    ActivityType activityType;
    String typeId;
    public Singleton() {
    }
    public Singleton(ActivityType activityType) {
      this.activityType = activityType;
    }
    public Singleton(ActivityType activityType, String typeId) {
      this.activityType = activityType;
      this.typeId = typeId;
    }
    @Override
    public void register(ProcessEngineImpl processEngine, ActivityTypeService activityTypeService) {
      if (typeId==null) {
        this.typeId = getJsonTypeName(activityType);
      }
      activityTypeService.registerSingletonActivityType(activityType, typeId);
    }
    public ActivityType getActivityType() {
      return activityType;
    }
    public void setActivityType(ActivityType activityType) {
      this.activityType = activityType;
    }
    public String getTypeId() {
      return typeId;
    }
    public void setTypeId(String typeId) {
      this.typeId = typeId;
    }
  }
  
  public static class Configurable extends ActivityTypeRegistration {
    ActivityType activityType;
    public Configurable() {
    }
    public Configurable(ActivityType activityType) {
      this.activityType = activityType;
    }
    @Override
    public void register(ProcessEngineImpl processEngine, ActivityTypeService activityTypeService) {
      activityTypeService.registerConfigurableActivityType(activityType);
    }
    public ActivityType getActivityType() {
      return activityType;
    }
    public void setActivityType(ActivityType activityType) {
      this.activityType = activityType;
    }
  }
}

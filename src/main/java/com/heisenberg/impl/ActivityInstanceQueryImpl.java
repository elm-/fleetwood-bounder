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

import com.heisenberg.api.builder.ActivityInstanceQuery;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.util.Page;
import com.heisenberg.impl.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class ActivityInstanceQueryImpl implements ActivityInstanceQuery {
  
  ProcessEngineImpl processEngine;
  Class< ? > activityTypeClass;
  
  public ActivityInstanceQueryImpl(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public ActivityInstanceQuery activityType(Class< ? > activityTypeClass) {
    this.activityTypeClass = activityTypeClass;
    return this;
  }

  @Override
  public Page<ActivityInstance> loadPage() {
    return processEngine.findActivityInstances(this);
  }

  public boolean meetsConditions(ActivityInstanceImpl activityInstance) {
    if (activityTypeClass!=null) {
      if (activityInstance.activityDefinition.activityType.getClass()==activityTypeClass) {
        return true;
      }
      return false;
    } else {
      return true;
    }
  }

}

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
package com.heisenberg.spi;

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.instance.ActivityInstanceImpl;



/**
 * @author Walter White
 */
public abstract class ActivityType implements Spi {

  /** The unique id for this activity type. */
  public abstract String getId();

  public abstract void start(ActivityInstanceImpl activityInstance);

  /** Specifies the input or output parameters for this activity (if any). 
   * Invoked just once during initialization of the process engine.
   * If you return activity parameters, you could consider overriding the default 
   * parameter checking in {@link #checkParameters(ActivityDefinitionImpl, DeployProcessDefinitionResponse)}. */
  public ActivityParameter[] getActivityParameters() {
    return null;
  }

  public void signal(ActivityInstanceImpl activityInstance) {
  }
}

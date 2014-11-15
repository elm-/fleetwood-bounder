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

import java.util.List;
import java.util.Map;

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.definition.ParameterInstance;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ParameterInstanceImpl;
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
  public List<ActivityParameter> getActivityParameters() {
    return null;
  }

  /** Checks the parameters during process deployment.
   * The errors and warnings should be reported with response.addError() and response.addWarning(). */
  public void checkParameters(ActivityDefinitionImpl activityDefinitionImpl, Map<String,ActivityParameter> activityParameterMap, DeployProcessDefinitionResponse response) {
    if (activityParameterMap!=null) {
      Map<String,ParameterInstanceImpl> parameterInstances = ParameterInstance.buildParameterInstancesMap(activityDefinitionImpl.parameterInstances);
      for (String activityParameterName: activityParameterMap.keySet()) {
        ActivityParameter activityParameter = activityParameterMap.get(activityParameterName); 
        activityParameter.checkParameters(parameterInstances, response);
      }
    }
  }

  public void signal(ActivityInstanceImpl activityInstance) {
  }
}

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

import java.util.HashMap;
import java.util.Map;

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.definition.Location;
import com.heisenberg.definition.ParameterInstanceImpl;



/**
 * @author Walter White
 */
public abstract class ActivityParameter {

  public String name;
  public Type type;
  public Boolean required;
  public Boolean recommended;
  
  public ActivityParameter(Type type) {
    this.type = type;
  }
  
  public ActivityParameter name(String name) {
    this.name = name;
    return this;
  }
  
  /** generates an error if the parameter is not provided */
  public ActivityParameter required() {
    this.required = true;
    return this;
  }

  /** generates a warning if the parameter is not provided */
  public ActivityParameter recommended() {
    this.required = true;
    return this;
  }

  public void checkParameters(Location activityDefinitionLocation, Map<String, ParameterInstanceImpl> parameterInstances, DeployProcessDefinitionResponse response) {
    if (!parameterInstances.containsKey(name)) {
      if (Boolean.TRUE.equals(required)) {
        response.addError(activityDefinitionLocation, "Parameter %s is not provided", name);
      } else if (Boolean.TRUE.equals(recommended)) {
        response.addWarning(activityDefinitionLocation, "Parameter %s is not provided", name);
      }
    }
  }

  public static Map<String, ActivityParameter> buildActivityParameterMap(ActivityParameter[] activityParameters) {
    Map<String, ActivityParameter> activityParameterMap = new HashMap<>();
    if (activityParameters!=null) {
      for (ActivityParameter activityParameter: activityParameters) {
        activityParameterMap.put(activityParameter.name, activityParameter);
      }
    }
    return activityParameterMap;
  }
}

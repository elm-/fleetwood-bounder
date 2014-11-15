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
package com.heisenberg.api.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.definition.ParameterInstanceImpl;


/**
 * @author Walter White
 */
public class ParameterInstance {

  public String parameterRefName;
  public List<ParameterBinding> parameterBindings;
  public Location location;

  public ParameterInstance parameterRefName(String parameterRefName) {
    this.parameterRefName = parameterRefName;
    return this;
  }
  
  public ParameterInstance parameterBinding(ParameterBinding parameterBinding) {
    if (parameterBindings==null) {
      parameterBindings = new ArrayList<ParameterBinding>();
    }
    if (parameterBinding.location==null) {
      parameterBinding.location = new Location();
    } 
    if (parameterBinding.location.path==null) {
      parameterBinding.location.path = location.path+"["+parameterBindings.size()+"]";
    }
    parameterBindings.add(parameterBinding);
    return this;
  }

  public static Map<String,ParameterInstanceImpl> buildParameterInstancesMap(List<ParameterInstanceImpl> parameterInstances) {
    Map<String,ParameterInstanceImpl> parameterInstancesMap = new HashMap<>();
    if (parameterInstances!=null) {
      for (ParameterInstanceImpl parameterInstance: parameterInstances) {
        parameterInstancesMap.put(parameterInstance.name, parameterInstance);
      }
    }
    return parameterInstancesMap;
  }
}

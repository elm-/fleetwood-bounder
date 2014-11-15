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
package com.heisenberg.definition;

import java.util.LinkedHashMap;
import java.util.Map;

import com.heisenberg.spi.ActivityParameter;


/**
 * @author Walter White
 */
public class ParameterDefinitionsImpl {

  Map<String, ActivityParameter> parameterDefinitions;

  public ParameterDefinitionsImpl(ActivityParameter... parameterDefinitions) {
    this.parameterDefinitions = new LinkedHashMap<>();
    if (parameterDefinitions!=null) {
      for (ActivityParameter parameterDefinition: parameterDefinitions) {
        this.parameterDefinitions.put(parameterDefinition.name, parameterDefinition);
      }
    }
  }
  
  public ActivityParameter getParameterDefinition(String name) {
    return (parameterDefinitions!=null ? parameterDefinitions.get(name) : null);
  }
}

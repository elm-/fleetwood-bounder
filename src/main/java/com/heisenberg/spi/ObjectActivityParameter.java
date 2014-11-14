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

import com.heisenberg.definition.ParameterBindingImpl;
import com.heisenberg.definition.ParameterInstanceImpl;
import com.heisenberg.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class ObjectActivityParameter extends ActivityParameter {

  ObjectActivityParameter(Type type) {
    super(type);
  }

  public static ObjectActivityParameter type(Type type) {
    return new ObjectActivityParameter(type);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(ActivityInstance activityInstance, Class<T> valueType) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    ParameterInstanceImpl parameterInstance = activityInstanceImpl.getActivityDefinition().findParameterInstance(id);
    if ( parameterInstance==null 
         || parameterInstance.parameterBindings==null
         || parameterInstance.parameterBindings.isEmpty()) {
      return null;
    }
    ParameterBindingImpl parameterBinding = parameterInstance.parameterBindings.get(0);
    return (T) parameterBinding.getValue(activityInstanceImpl);
  }

  @Override
  public ObjectActivityParameter id(String id) {
    super.id(id);
    return this;
  }
  
  
}

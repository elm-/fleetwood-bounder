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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.heisenberg.definition.ParameterBindingImpl;
import com.heisenberg.definition.ParameterInstanceImpl;
import com.heisenberg.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class ListActivityParameter extends ActivityParameter {
  
  ListActivityParameter(Type type) {
    super(type);
  }

  public static ListActivityParameter type(Type type) {
    return new ListActivityParameter(type);
  }

  @SuppressWarnings("unchecked")
  public <T> List<T> get(ActivityInstanceImpl activityInstance, Class<T> valueType) {
    ParameterInstanceImpl parameterInstance = activityInstance.getActivityDefinition().findParameterInstance(name);
    if ( parameterInstance==null 
         || parameterInstance.parameterBindings==null
         || parameterInstance.parameterBindings.isEmpty()) {
      return null;
    }
    List<T> values = new ArrayList<T>();
    for (ParameterBindingImpl parameterBinding: parameterInstance.parameterBindings) {
      Object value = parameterBinding.getValue(activityInstance);
      if (value instanceof Collection) {
        for (Object element: (Collection<Object>)value) {
          values.add((T)element);
        }
      } else {
        values.add((T) value);
      }
    }
    return values;
  }

  @Override
  public ListActivityParameter name(String id) {
    super.name(id);
    return this;
  }

}

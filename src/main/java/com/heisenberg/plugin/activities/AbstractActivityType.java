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
package com.heisenberg.plugin.activities;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.slf4j.Logger;

import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.plugin.Descriptors;
import com.heisenberg.plugin.TypeField;
import com.heisenberg.plugin.Validator;




/**
 * @author Walter White
 */
public abstract class AbstractActivityType implements ActivityType {
  
  public static final Logger log = ProcessEngineImpl.log;
  
  public abstract void start(ControllableActivityInstance activityInstance);

  public void message(ControllableActivityInstance activityInstance) {
    activityInstance.onwards();
  }
  
  public void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance) {
    if (!activityInstance.hasOpenActivityInstances()) {
      activityInstance.end();
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void validate(ActivityDefinition activityDefinition, Validator validator) {
    Descriptors activityTypeService = validator.getServiceRegistry().getService(Descriptors.class);
    List<TypeField> configurationFields = activityTypeService.getConfigurationFields(this);
    if (configurationFields!=null) {
      for (TypeField typeField : configurationFields) {
        Field field = typeField.field;
        try {
          Object value = field.get(this);
          if (value==null) {
            if (Boolean.TRUE.equals(typeField.isRequired)) {
              validator.addError("Configuration field %s is required", typeField.label);
            }
          }
          if (value instanceof Binding) {
            validateBinding(activityDefinition, validator, typeField, (Binding< ? >) value);
          } else if (isListOfBindings(field)) {
            List<Binding> bindings = (List<Binding>) value;
            if (bindings!=null) {
              for (Binding binding: bindings) {
                validateBinding(activityDefinition, validator, typeField, binding);
              }
            }
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private boolean isListOfBindings(Field field) {
    Type genericType = field.getGenericType();
    if (! (genericType instanceof ParameterizedType)) {
      return false;
    }
    ParameterizedType parameterizedType = (ParameterizedType) genericType;
    if ( List.class.isAssignableFrom((Class<?>)parameterizedType.getRawType())
         && parameterizedType.getActualTypeArguments().length==1 ) {
      Type listParameter = parameterizedType.getActualTypeArguments()[0];
      Class<?> rawListParameter = (Class<?>) (listParameter instanceof ParameterizedType ? ((ParameterizedType)listParameter).getRawType() : listParameter);
      return Binding.class.equals(rawListParameter);
    }
    return false;
  }

  private void validateBinding(ActivityDefinition activityDefinition, Validator validator, TypeField typeField, Binding< ? > binding) {
    binding.dataType = typeField.dataType;
    binding.validate(activityDefinition, validator, this.getClass().getName()+"."+typeField.name);
  }
}

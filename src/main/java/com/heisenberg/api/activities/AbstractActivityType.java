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
package com.heisenberg.api.activities;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;

import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.plugin.TypeField;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.plugin.ActivityTypeService;




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

  @Override
  public void validate(ActivityDefinition activityDefinition, Validator validator) {
    validateConfigurationFields(activityDefinition, validator);
  }

  protected void validateConfigurationFields(ActivityDefinition activityDefinition, Validator validator) {
    ActivityTypeService activityTypeService = (ActivityTypeService) validator.getServiceLocator().getActivityTypes();
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
            Binding< ? > binding = (Binding< ? >) value;
            binding.dataType = typeField.dataType;
            binding.validate(activityDefinition, this, typeField, validator);
          }
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}

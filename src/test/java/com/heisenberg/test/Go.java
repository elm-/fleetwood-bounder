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
package com.heisenberg.test;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
@JsonTypeName("go")
public class Go extends AbstractActivityType {
  
  public List<Execution> executions = new ArrayList<>();
  
  @ConfigurationField("Place")
  Binding<String> placeBinding;

  public Go placeBinding(Binding<String> placeBinding) {
    this.placeBinding = placeBinding;
    return this;
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    String place = (placeBinding!=null ? placeBinding.getValue(activityInstance) : null);
    executions.add(new Execution(activityInstance, place));
    activityInstance.onwards();
  }
  
  @Override
  public void validate(ActivityDefinition activityDefinition, Validator validator) {
    activityDefinition.initializeBindings(validator);
  }

  @Override
  public void signal(ControllableActivityInstance activityInstance) {
  }

  public class Execution {
    public ControllableActivityInstance activityInstance;
    public String place;
    public Execution(ControllableActivityInstance activityInstance, String place) {
      this.activityInstance = activityInstance;
      this.place = place;
    }
  }
  
  
}

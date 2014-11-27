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

import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.util.Validator;


/**
 * @author Walter White
 */
public class Wait implements ActivityType {
  
  public static final Wait INSTANCE = new Wait();

  @Override
  public String getTypeId() {
    return "wait";
  }

  @Override
  public String getLabel() {
    return "Wait";
  }

  public static List<Execution> executions = new ArrayList<>();

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    executions.add(new Execution(activityInstance));
  }

  @Override
  public void notify(ControllableActivityInstance activityInstance) {
    activityInstance.onwards();
  }

  public class Execution {
    public ControllableActivityInstance activityInstance;
    public Execution(ControllableActivityInstance activityInstance) {
      this.activityInstance = activityInstance;
    }
  }

  @Override
  public void validate(ActivityDefinition activity, Validator validator) {
  }

  @Override
  public void ended(ControllableActivityInstance activityInstance, ActivityInstance nestedEndedActivityInstance) {
  }
}

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
package com.heisenberg.impl.engine.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.heisenberg.api.util.ActivityInstanceId;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.engine.updates.OperationAddUpdate;
import com.heisenberg.impl.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public abstract class Operation {

  @JsonIgnore
  public ActivityInstanceImpl activityInstance;
  
  public ActivityInstanceId activityInstanceId;

  public Operation() {
  }

  public Operation(ActivityInstanceImpl activityInstance) {
    this.activityInstance = activityInstance;
  }

  public abstract boolean isAsync();

  public abstract void execute(ProcessEngineImpl processEngine);
  
  public abstract OperationAddUpdate getUpdate();

  public ActivityInstanceImpl getActivityInstance() {
    return activityInstance;
  }

  public void setActivityInstance(ActivityInstanceImpl activityInstance) {
    this.activityInstance = activityInstance;
  }
}

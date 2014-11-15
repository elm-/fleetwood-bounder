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
package com.heisenberg.engine.operation;

import com.heisenberg.definition.ScopeDefinitionImpl;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class NotifyActivityInstanceEndToParent implements Operation {

  public static final String FIELD_ACTIVITY_INSTANCE_ID = "activityInstanceId";
  protected ActivityInstanceImpl activityInstance;
  
  public NotifyActivityInstanceEndToParent(ActivityInstanceImpl activityInstance) {
    this.activityInstance = activityInstance;
  }

  @Override
  public void execute(ProcessEngineImpl processEngine) {
    ScopeDefinitionImpl parentDefinition = activityInstance.getParent().getScopeDefinition();
    parentDefinition.notifyActivityInstanceEnded(activityInstance);
  }
  
  public ActivityInstanceImpl getActivityInstance() {
    return activityInstance;
  }

  public void setActivityInstance(ActivityInstanceImpl activityInstance) {
    this.activityInstance = activityInstance;
  }

  @Override
  public boolean isAsync() {
    return false;
  }
}
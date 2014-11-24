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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.engine.updates.OperationAddActivityInstanceStartUpdate;
import com.heisenberg.impl.engine.updates.OperationAddUpdate;
import com.heisenberg.impl.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class ActivityInstanceStartOperation extends Operation {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public ActivityInstanceStartOperation() {
  }

  public ActivityInstanceStartOperation(ActivityInstanceImpl activityInstance) {
    super(activityInstance);
  }

  @Override
  public boolean isAsync() {
    return activityInstance.getActivityDefinition().isAsync(activityInstance);
  }

  public void execute(ProcessEngineImpl processEngine) {
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    log.debug("Starting "+activityInstance);
    activityDefinition.activityType.start(activityInstance);
  }

  @Override
  public OperationAddUpdate getUpdate() {
    return new OperationAddActivityInstanceStartUpdate(activityInstance);
  }
}

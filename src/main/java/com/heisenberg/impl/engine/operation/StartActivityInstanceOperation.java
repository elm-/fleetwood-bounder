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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.engine.updates.OperationAddStartUpdate;
import com.heisenberg.impl.engine.updates.OperationAddUpdate;
import com.heisenberg.impl.instance.ActivityInstanceImpl;


/**
 * @author Walter White
 */
public class StartActivityInstanceOperation extends Operation {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);
  
  Boolean isForEachElement;

  public StartActivityInstanceOperation() {
  }

  public StartActivityInstanceOperation(ActivityInstanceImpl activityInstance) {
    super(activityInstance);
  }

  public StartActivityInstanceOperation(ActivityInstanceImpl activityInstance, boolean isForEachElement) {
    super(activityInstance);
    this.isForEachElement = isForEachElement;
  }

  @Override
  public boolean isAsync() {
    return activityInstance.getActivityDefinition().isAsync(activityInstance);
  }

  public void execute(ProcessEngineImpl processEngine) {
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    if (activityDefinition.forEach!=null && !Boolean.TRUE.equals(isForEachElement)) {
      List<Object> values = activityInstance.getValue(activityDefinition.forEach);
      if (values!=null && !values.isEmpty()) {
        for (Object value: values) {
          ActivityInstanceImpl elementActivityInstance = activityInstance.createActivityInstance(activityDefinition);
          elementActivityInstance.initializeForEachElement(activityDefinition.forEachElement, value);
          activityInstance.processInstance.addOperation(new StartActivityInstanceOperation(elementActivityInstance, true));
        }
      } else {
        activityInstance.onwards();
      }
    } else {
      log.debug("Starting "+activityInstance);
      activityDefinition.activityType.start(activityInstance);
    }
  }

  @Override
  public OperationAddUpdate createUpdate() {
    return new OperationAddStartUpdate(activityInstance);
  }
}

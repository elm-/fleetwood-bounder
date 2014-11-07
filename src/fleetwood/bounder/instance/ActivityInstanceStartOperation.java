/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.instance;

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.engine.updates.ActivityInstanceStartUpdate;
import fleetwood.bounder.util.Time;


/**
 * @author Walter White
 */
public class ActivityInstanceStartOperation implements Operation {
  
  protected ActivityInstance activityInstance;

  public ActivityInstanceStartOperation(ActivityInstance activityInstance) {
    this.activityInstance = activityInstance;
  }

  public void execute() {
    ProcessInstance processInstance = activityInstance.processInstance;
    activityInstance.setStart(Time.now());
    processInstance.addUpdate(new ActivityInstanceStartUpdate(activityInstance));
    ActivityDefinition activityDefinition = activityInstance.getActivityDefinition();
    ProcessEngineImpl.log.debug("Starting "+activityInstance);
    activityDefinition.start(activityInstance);
  }

  
  public ActivityInstance getActivityInstance() {
    return activityInstance;
  }

  
  public void setActivityInstance(ActivityInstance activityInstance) {
    this.activityInstance = activityInstance;
  }
}

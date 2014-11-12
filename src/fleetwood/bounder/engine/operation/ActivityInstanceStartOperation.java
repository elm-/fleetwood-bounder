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

package fleetwood.bounder.engine.operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ProcessEngineImpl;
import fleetwood.bounder.json.JsonReader;
import fleetwood.bounder.json.JsonTypeId;
import fleetwood.bounder.json.JsonWriter;


/**
 * @author Walter White
 */
@JsonTypeId("aiStart")
public class ActivityInstanceStartOperation implements Operation {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public static final String FIELD_ACTIVITY_INSTANCE_ID = "activityInstanceId";
  protected ActivityInstance activityInstance;

  public ActivityInstanceStartOperation(ActivityInstance activityInstance) {
    this.activityInstance = activityInstance;
  }

  @Override
  public boolean isAsync() {
    return activityInstance.getActivityDefinition().isAsync(activityInstance);
  }

  public void execute(ProcessEngineImpl processEngine) {
    ActivityDefinition activityDefinition = activityInstance.getActivityDefinition();
    log.debug("Starting "+activityInstance);
    activityDefinition.start(activityInstance);
  }
  
  public ActivityInstance getActivityInstance() {
    return activityInstance;
  }
  
  public void setActivityInstance(ActivityInstance activityInstance) {
    this.activityInstance = activityInstance;
  }

  @Override
  public void write(JsonWriter writer) {
    writer.writeObjectStart(this);
    writer.writeIdField(FIELD_ACTIVITY_INSTANCE_ID, activityInstance!=null ? activityInstance.getId() : null);
    writer.writeObjectEnd(this);
  }

  @Override
  public void read(JsonReader reader) {
  }
}

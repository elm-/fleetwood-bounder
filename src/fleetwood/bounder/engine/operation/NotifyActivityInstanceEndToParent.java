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

import fleetwood.bounder.definition.CompositeDefinition;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ProcessEngineImpl;
import fleetwood.bounder.json.JsonSerializer;


/**
 * @author Walter White
 */
public class NotifyActivityInstanceEndToParent implements Operation {

  public static final String TYPE_NOTIFY_ACTIVITY_INSTANCE_END_TO_PARENT = "notifyEndToParent";

  public static final String FIELD_ACTIVITY_INSTANCE_ID = "activityInstanceId";
  protected ActivityInstance activityInstance;
  
  public NotifyActivityInstanceEndToParent(ActivityInstance activityInstance) {
    this.activityInstance = activityInstance;
  }

  @Override
  public void execute(ProcessEngineImpl processEngine) {
    CompositeDefinition parentDefinition = activityInstance.getParent().getCompositeDefinition();
    parentDefinition.notifyActivityInstanceEnded(activityInstance);
  }
  
  public ActivityInstance getActivityInstance() {
    return activityInstance;
  }

  public void setActivityInstance(ActivityInstance activityInstance) {
    this.activityInstance = activityInstance;
  }

  @Override
  public boolean isAsync() {
    return false;
  }

  @Override
  public String getSerializableType() {
    return TYPE_NOTIFY_ACTIVITY_INSTANCE_END_TO_PARENT;
  }

  @Override
  public void serialize(JsonSerializer serializer) {
    serializer.objectStart(this);
    serializer.writeIdField(FIELD_ACTIVITY_INSTANCE_ID, activityInstance!=null ? activityInstance.getId() : null);
    serializer.objectEnd(this);
  }
}

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

package fleetwood.bounder.engine.updates;

import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.json.JsonSerializer;


/**
 * @author tbaeyens
 */
public class ActivityInstanceCreateUpdate extends ActivityInstanceUpdate {

  public static final String TYPE_ACTIVITY_INSTANCE_CREATE_UPDATE = "aiCreate";
  public static final String FIELD_ACTIVITY_INSTANCE_CREATED = "activityInstance";

  public ActivityInstanceCreateUpdate(ActivityInstance activityInstance) {
    super(activityInstance);
  }

  @Override
  public String getSerializableType() {
    return TYPE_ACTIVITY_INSTANCE_CREATE_UPDATE;
  }

  @Override
  public void serialize(JsonSerializer serializer) {
    serializer.objectStart(this);
    serializer.writeObject(FIELD_ACTIVITY_INSTANCE_CREATED, activityInstance);
    serializer.objectEnd(this);
  }
}

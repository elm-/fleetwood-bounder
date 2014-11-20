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
package com.heisenberg.bpmn.activities;

import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.Spi;


/**
 * @author Walter White
 */
public class StartEvent extends ActivityType {

  public static final Spi INSTANCE = new StartEvent();

  @Override
  public String getId() {
    return "startEvent";
  }

  @Override
  public String getLabel() {
    return "Start event";
  }

  @Override
  public void start(ActivityInstanceImpl activityInstance) {
    activityInstance.onwards();
  }
}

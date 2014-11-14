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
package com.heisenberg;

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.ActivityInstance;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.util.Id;


/**
 * @author Walter White
 */
public class Wait implements ActivityType {
  
  public static final String ID = "wait";
  
  protected List<k> activityInstances = new ArrayList<>();

  @Override
  public void start(ActivityInstance activityInstance) {
    activityInstances.add(activityInstance);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public void signal(ActivityInstance activityInstance) {
  }
  
  
}

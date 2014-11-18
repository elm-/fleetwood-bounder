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
import com.heisenberg.spi.ActivityType;


/**
 * @author Walter White
 */
public class Wait extends ActivityType {
  
  public static final String ID = "wait";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getLabel() {
    return "Wait";
  }

  public static List<Execution> executions = new ArrayList<>();

  @Override
  public void start(ActivityInstanceImpl activityInstance) {
    executions.add(new Execution(activityInstance));
  }

  @Override
  public void signal(ActivityInstanceImpl activityInstance) {
    activityInstance.onwards();
  }

  public class Execution {
    public ActivityInstanceImpl activityInstance;
    public Execution(ActivityInstanceImpl activityInstance) {
      this.activityInstance = activityInstance;
    }
  }
}

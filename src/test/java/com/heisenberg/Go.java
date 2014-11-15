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
import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.ObjectActivityParameter;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class Go extends ActivityType {
  
  public static final String ID = "go";

  @Override
  public String getId() {
    return ID;
  }

  public static ObjectActivityParameter PLACE = ObjectActivityParameter
          .type(Type.TEXT)
          .name("place");
        
  @Override
  public ActivityParameter[] getActivityParameters() {
    return new ActivityParameter[]{PLACE};
  }

  public static List<Execution> executions = new ArrayList<>();
  
  @Override
  public void start(ActivityInstanceImpl controller) {
    String place = PLACE.get(controller, String.class);
    executions.add(new Execution(controller, place));
    controller.onwards();
  }

  @Override
  public void signal(ActivityInstanceImpl activityInstance) {
  }

  public class Execution {
    public ActivityInstanceImpl activityInstance;
    public String place;
    public Execution(ActivityInstanceImpl activityInstance, String place) {
      this.activityInstance = activityInstance;
      this.place = place;
    }
  }
}

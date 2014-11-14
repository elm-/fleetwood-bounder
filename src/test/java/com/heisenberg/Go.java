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

import com.heisenberg.spi.ActivityInstance;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.ObjectActivityParameter;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class Go implements ActivityType {
  
  protected static List<Execution> executions = new ArrayList<>();

  // TODO scan the parameters with reflection
  
  public static final String ID = "go";
  
  public static ObjectActivityParameter PLACE = ObjectActivityParameter
    .type(Type.TEXT)
    .id("place");
  
  @Override
  public void start(ActivityInstance activityInstance) {
    String place = PLACE.get(activityInstance, String.class);
    executions.add(new Execution(activityInstance, place));
    activityInstance.onwards();
  }

  @Override
  public void signal(ActivityInstance activityInstance) {
  }

  @Override
  public String getId() {
    return ID;
  }
  
  public class Execution {
    public ActivityInstance activityInstance;
    public String place;
    public Execution(ActivityInstance activityInstance, String place) {
      this.activityInstance = activityInstance;
      this.place = place;
    }
  }
}

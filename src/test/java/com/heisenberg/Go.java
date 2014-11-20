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

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.spi.AbstractActivityType;
import com.heisenberg.spi.Binding;


/**
 * @author Walter White
 */
@JsonTypeName("go")
public class Go extends AbstractActivityType {
  
  public static List<Execution> executions = new ArrayList<>();
  
  Binding<String> placeBinding;

  public Go placeBinding(Binding<String> placeBinding) {
    this.placeBinding = placeBinding;
    return this;
  }

  @Override
  public void start(ActivityInstanceImpl activityInstance) {
    String place = placeBinding.getValue(activityInstance);
    executions.add(new Execution(activityInstance, place));
    activityInstance.onwards();
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

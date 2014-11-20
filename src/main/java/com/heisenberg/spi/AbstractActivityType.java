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
package com.heisenberg.spi;

import com.heisenberg.instance.ActivityInstanceImpl;



/**
 * @author Walter White
 */
public abstract class AbstractActivityType implements ActivityType {
  
  public abstract void start(ActivityInstanceImpl activityInstance);

  public void signal(ActivityInstanceImpl activityInstance) {
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getLabel() {
    return null;
  }
}

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
import com.heisenberg.spi.AbstractActivityType;
import com.heisenberg.spi.ControllableActivityInstance;


/**
 * @author Walter White
 */
public abstract class ServiceTask extends AbstractActivityType {

  public static final String ID = "serviceTask";

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    invokeService(activityInstance);
    activityInstance.onwards();
  }
  
  public abstract void invokeService(ControllableActivityInstance activityInstance);
}

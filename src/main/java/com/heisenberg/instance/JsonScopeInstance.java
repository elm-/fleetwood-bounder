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
package com.heisenberg.instance;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Walter White
 */
public class JsonScopeInstance {

  String id;
  Long start;
  Long end;
  Long duration;

  List<JsonActivityInstance> activityInstances;

  List<JsonVariableInstance> variableInstances;
  
  public JsonScopeInstance() {
  }
  public JsonScopeInstance(ScopeInstance scopeInstance) {
    this.start = scopeInstance.start;
    this.end = scopeInstance.end;
    this.duration = scopeInstance.duration;
    if (scopeInstance.activityInstances!=null) {
      this.activityInstances = new ArrayList<JsonActivityInstance>(scopeInstance.activityInstances.size());
      for (ActivityInstance activityInstance: scopeInstance.activityInstances) {
        this.activityInstances.add(new JsonActivityInstance(activityInstance));
      }
    }
    if (scopeInstance.variableInstances!=null) {
      this.variableInstances = new ArrayList<JsonVariableInstance>(scopeInstance.variableInstances.size());
      for (VariableInstance variableInstance: scopeInstance.variableInstances) {
        this.variableInstances.add(new JsonVariableInstance(variableInstance));
      }
    }
  }
}

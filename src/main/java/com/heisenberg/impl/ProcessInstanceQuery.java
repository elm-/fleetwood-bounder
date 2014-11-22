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
package com.heisenberg.impl;

import java.util.List;

import com.heisenberg.api.util.ActivityInstanceId;
import com.heisenberg.api.util.ProcessInstanceId;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;


/**
 * @author Walter White
 */
public class ProcessInstanceQuery {

  protected ProcessEngineImpl processEngine;
  protected ProcessInstanceId processInstanceId;
  protected ActivityInstanceId activityInstanceId;
  protected Integer maxResults;
  
  public ProcessInstanceQuery(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public ActivityInstanceId getActivityInstanceId() {
    return activityInstanceId;
  }
  
  public void setActivityInstanceId(ActivityInstanceId activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
  
  public ProcessInstanceId getProcessInstanceId() {
    return processInstanceId;
  }
  
  public void setProcessInstanceId(ProcessInstanceId processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  public Integer getMaxResults() {
    return maxResults;
  }
  
  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  public boolean meetsConditions(ProcessInstanceImpl processInstance) {
    if (activityInstanceId!=null && !containsCompositeInstance(processInstance, activityInstanceId)) {
      return false;
    }
    return true;
  }

  boolean containsCompositeInstance(ScopeInstanceImpl scopeInstance, ActivityInstanceId activityInstanceId) {
    if (scopeInstance.hasActivityInstances()) {
      for (ActivityInstanceImpl activityInstance : scopeInstance.getActivityInstances()) {
        if (containsActivityInstance(activityInstance, activityInstanceId)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean containsActivityInstance(ActivityInstanceImpl activityInstance, ActivityInstanceId activityInstanceId) {
    if (activityInstanceId.equals(activityInstance.getId())) {
      return true;
    }
    return containsCompositeInstance(activityInstance, activityInstanceId);
  }
  
  public ProcessInstanceQuery processInstanceId(ProcessInstanceId id) {
    setProcessInstanceId(id);
    return this;
  }
  
  public ProcessInstanceQuery activityInstanceId(ActivityInstanceId id) {
    setActivityInstanceId(id);
    return this;
  }
  
  public ProcessInstanceImpl get() {
    setMaxResults(1);
    List<ProcessInstanceImpl> processInstances = asList();
    if (processInstances!=null && !processInstances.isEmpty()) {
      return processInstances.get(0);
    }
    return null;
  }

  public List<ProcessInstanceImpl> asList() {
    return processEngine.findProcessInstances(this);
  }
}

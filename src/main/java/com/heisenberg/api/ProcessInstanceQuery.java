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
package com.heisenberg.api;

import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.ActivityInstanceId;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.instance.ProcessInstanceId;
import com.heisenberg.instance.ScopeInstanceImpl;


/**
 * @author Walter White
 */
public class ProcessInstanceQuery {

  protected ProcessInstanceId processInstanceId;
  protected ActivityInstanceId activityInstanceId;
  protected Integer maxResults;

  
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

  public boolean satisfiesCriteria(ProcessInstanceImpl processInstance) {
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
}

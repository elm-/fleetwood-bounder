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

import com.heisenberg.api.builder.WorkflowInstanceQuery;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;


/**
 * @author Walter White
 */
public class WorkflowInstanceQueryImpl implements WorkflowInstanceQuery {

  public WorkflowInstanceStore workflowInstanceStore;
  public String workflowInstanceId;
  public String activityInstanceId;
  public Integer maxResults;
  
  public WorkflowInstanceQueryImpl(WorkflowInstanceStore workflowInstanceStore) {
    this.workflowInstanceStore = workflowInstanceStore;
  }

  public WorkflowInstanceQueryImpl workflowInstanceId(String workflowInstanceId) {
    setWorkflowInstanceId(workflowInstanceId);
    return this;
  }
  
  public WorkflowInstanceQueryImpl activityInstanceId(String activityInstanceId) {
    setActivityInstanceId(activityInstanceId);
    return this;
  }
  
  public Object getActivityInstanceId() {
    return activityInstanceId;
  }
  
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }
  
  public Object String() {
    return workflowInstanceId;
  }
  
  public void setWorkflowInstanceId(String processInstanceId) {
    this.workflowInstanceId = processInstanceId;
  }
  
  public Integer getMaxResults() {
    return maxResults;
  }
  
  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  public WorkflowInstanceImpl get() {
    setMaxResults(1);
    List<WorkflowInstanceImpl> workflowInstances = asList();
    if (workflowInstances!=null && !workflowInstances.isEmpty()) {
      return workflowInstances.get(0);
    }
    return null;
  }

  public List<WorkflowInstanceImpl> asList() {
    return workflowInstanceStore.findWorkflowInstances(this);
  }
}

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

import com.heisenberg.api.builder.ProcessInstanceQuery;
import com.heisenberg.impl.instance.ProcessInstanceImpl;


/**
 * @author Walter White
 */
public class ProcessInstanceQueryImpl implements ProcessInstanceQuery {

  public WorkflowInstanceStore workflowInstanceStore;
  public String processInstanceId;
  public String activityInstanceId;
  public Integer maxResults;
  
  public ProcessInstanceQueryImpl(WorkflowInstanceStore workflowInstanceStore) {
    this.workflowInstanceStore = workflowInstanceStore;
  }

  public ProcessInstanceQueryImpl processInstanceId(String processInstanceId) {
    setProcessInstanceId(processInstanceId);
    return this;
  }
  
  public ProcessInstanceQueryImpl activityInstanceId(String activityInstanceId) {
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
    return processInstanceId;
  }
  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
  
  public Integer getMaxResults() {
    return maxResults;
  }
  
  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
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
    return workflowInstanceStore.findProcessInstances(this);
  }
}

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
package com.heisenberg.memory;

import java.util.List;

import com.heisenberg.api.task.Task;
import com.heisenberg.plugin.activities.ControllableActivityInstance;


/**
 * @author Walter White
 */
public class TaskImpl implements Task {
  
  public String id;
  public String name;
  public String assigneeId;
  public List<String> candidateIds;
  public Object activityInstanceId;
  public String processInstanceId;

  @Override
  public Task name(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public Task assigneeId(String assigneeId) {
    this.assigneeId = assigneeId;
    return this;
  }

  @Override
  public Task candidateIds(List<String> candidateIds) {
    this.candidateIds = candidateIds;
    return this;
  }

  @Override
  public Task activityInstance(ControllableActivityInstance activityInstance) {
    this.activityInstanceId = activityInstance.getId();
    return this;
  }

  
  public Object getId() {
    return id;
  }

  
  public void setId(String id) {
    this.id = id;
  }

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public Object getAssigneeId() {
    return assigneeId;
  }

  
  public void setAssigneeId(String assigneeId) {
    this.assigneeId = assigneeId;
  }

  
  public List<String> getCandidateIds() {
    return candidateIds;
  }

  
  public void setCandidateIds(List<String> candidateIds) {
    this.candidateIds = candidateIds;
  }

  
  public Object getActivityInstanceId() {
    return activityInstanceId;
  }

  
  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  
  public Object getProcessInstanceId() {
    return processInstanceId;
  }

  
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }
}

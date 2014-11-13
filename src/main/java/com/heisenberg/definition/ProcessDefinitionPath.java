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
package com.heisenberg.definition;

import java.util.List;


/**
 * @author Walter White
 */
public class ProcessDefinitionPath {
  
  protected ProcessDefinitionId processDefinitionId;
  protected List<ActivityDefinitionId> activityDefinitionIds;

  public ProcessDefinitionPath(ProcessDefinitionId processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public ProcessDefinitionId getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(ProcessDefinitionId processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public List<ActivityDefinitionId> getActivityDefinitionIds() {
    return activityDefinitionIds;
  }

  public void setActivityDefinitionIds(List<ActivityDefinitionId> activityDefinitionIds) {
    this.activityDefinitionIds = activityDefinitionIds;
  }

  public ProcessDefinitionPath addActivityInstanceId(ActivityDefinitionId id) {
    return null;
  }

  /** slash separated path */
  public String toString() {
    if (processDefinitionId==null) {
      return "-path-without-process-definition-id-";
    }
    StringBuilder path = new StringBuilder();
    path.append(processDefinitionId);
    if (activityDefinitionIds!=null) {
      for (ActivityDefinitionId activityDefinitionId: activityDefinitionIds) {
        path.append("/");
        path.append(activityDefinitionId);
      }
    }
    return path.toString();
  }
}

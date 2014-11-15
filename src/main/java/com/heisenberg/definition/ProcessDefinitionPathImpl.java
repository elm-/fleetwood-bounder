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

import java.util.ArrayList;
import java.util.List;


/**
 * @author Walter White
 */
public class ProcessDefinitionPathImpl {
  
  public ProcessDefinitionId processDefinitionId;
  public List<String> activityDefinitionNames;

  public ProcessDefinitionPathImpl(ProcessDefinitionId processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public ProcessDefinitionPathImpl addActivityDefinitionName(String activiyDefinitionName) {
    if (activityDefinitionNames==null) {
      activityDefinitionNames = new ArrayList<>();
    }
    activityDefinitionNames.add(activiyDefinitionName);
    return null;
  }

  /** slash separated path */
  public String toString() {
    if (processDefinitionId==null) {
      return "-path-without-process-definition-id-";
    }
    StringBuilder path = new StringBuilder();
    path.append(processDefinitionId);
    if (activityDefinitionNames!=null) {
      for (String activityDefinitionName: activityDefinitionNames) {
        path.append("/");
        path.append(activityDefinitionName);
      }
    }
    return path.toString();
  }
}

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
package com.heisenberg.impl.definition;

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.util.ActivityDefinitionId;


/**
 * @author Walter White
 */
public class ProcessDefinitionPath {
  
  public List<String> activityDefinitionIds;

  public ProcessDefinitionPath addActivityDefinitionId(ActivityDefinitionId activiyDefinitionId) {
    if (activityDefinitionIds==null) {
      activityDefinitionIds = new ArrayList<>();
    }
    activityDefinitionIds.add(activiyDefinitionId.toString());
    return this;
  }

  /** slash separated path */
  public String toString() {
    if (activityDefinitionIds!=null) {
      StringBuilder path = new StringBuilder();
      for (String activityDefinitionId: activityDefinitionIds) {
        path.append("/");
        path.append(activityDefinitionId);
      }
      return path.toString();
    }
    return "/";
  }
}

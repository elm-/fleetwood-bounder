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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.heisenberg.impl.WorkflowQueryImpl;
import com.heisenberg.impl.WorkflowStore;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.plugin.ServiceRegistry;


/**
 * @author Walter White
 */
public class MemoryWorkflowStore implements WorkflowStore {

  protected Map<Object, WorkflowImpl> processDefinitions;

  public MemoryWorkflowStore() {
  }

  public MemoryWorkflowStore(ServiceRegistry serviceRegistry) {
    this.processDefinitions = new ConcurrentHashMap<Object, WorkflowImpl>();
  }

  /** ensures that every element in this process definition has an id */
  @Override
  public String createWorkflowId(WorkflowImpl processDefinition) {
    return UUID.randomUUID().toString();
  }

  @Override
  public void insertWorkflow(WorkflowImpl processDefinition) {
    processDefinitions.put(processDefinition.id, processDefinition);
  }

  @Override
  public List<WorkflowImpl> loadWorkflows(WorkflowQueryImpl query) {
    List<WorkflowImpl> result = null;
    if (query.id!=null) {
      result = new ArrayList<WorkflowImpl>();
      WorkflowImpl processDefinition = processDefinitions.get(query.id);
      if (processDefinition!=null) {
        result.add(processDefinition);
      }
    } else if (result==null) {
      result = new ArrayList<WorkflowImpl>(processDefinitions.values());
    }
    if (query.name!=null && !result.isEmpty()) {
      filterByName(result, query.name);
    }
    if (query.limit!=null) {
      while (result.size()>query.limit) {
        result.remove(result.size()-1);
      }
    }
    return result;
  }
  
  protected void filterByName(List<WorkflowImpl> result, String name) {
    for (int i=result.size()-1; i<=0; i--) {
      if (!name.equals(result.get(i).name)) {
        result.remove(i);
      }
    }
  }

  protected boolean matchesProcessDefinitionCriteria(WorkflowImpl process, WorkflowQueryImpl query) {
    if (query.name!=null && !query.name.equals(process.name)) {
      return false;
    }
    return true;
  }
}

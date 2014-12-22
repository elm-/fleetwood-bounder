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
package com.heisenberg.impl.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.heisenberg.impl.Time;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.WorkflowInstanceQueryImpl;
import com.heisenberg.impl.WorkflowInstanceStore;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.impl.util.Lists;


/**
 * @author Walter White
 */
public class MemoryWorkflowInstanceStore implements WorkflowInstanceStore {

  protected String workflowEngineId;
  protected Map<String, WorkflowInstanceImpl> workflowInstances;
  protected Set<String> lockedWorkflowInstances;
  
  public MemoryWorkflowInstanceStore() {
  }
  
  public MemoryWorkflowInstanceStore(ServiceRegistry serviceRegistry) {
    this.workflowInstances = new ConcurrentHashMap<>();
    this.lockedWorkflowInstances = Collections.synchronizedSet(new HashSet<String>());
    this.workflowEngineId = serviceRegistry.getService(WorkflowEngineImpl.class).getId();
  }

  @Override
  public String createWorkflowInstanceId(WorkflowImpl processDefinition) {
    return UUID.randomUUID().toString();
  }
  
  @Override
  public String createActivityInstanceId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public String createVariableInstanceId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public void insertWorkflowInstance(WorkflowInstanceImpl processInstance) {
    workflowInstances.put(processInstance.getId(), processInstance);
  }

  @Override
  public void flush(WorkflowInstanceImpl processInstance) {
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl processInstance) {
    lockedWorkflowInstances.remove(processInstance.getId());
    processInstance.removeLock();
  }
  
  @Override
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQueryImpl processInstanceQuery) {
    if (processInstanceQuery.workflowInstanceId!=null) {
      return Lists.of(workflowInstances.get(processInstanceQuery.workflowInstanceId));
    }
    List<WorkflowInstanceImpl> workflowInstances = new ArrayList<>();
    for (WorkflowInstanceImpl processInstance: this.workflowInstances.values()) {
      if (meetsConditions(processInstance, processInstanceQuery)) {
        workflowInstances.add(processInstance);
      }
    }
    return workflowInstances;
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceQueryImpl processInstanceQuery) {
    processInstanceQuery.setMaxResults(1);
    List<WorkflowInstanceImpl> workflowInstances = findWorkflowInstances(processInstanceQuery);
    if (workflowInstances==null || workflowInstances.isEmpty()) { 
      throw new RuntimeException("Process instance doesn't exist");
    }
    WorkflowInstanceImpl processInstance = workflowInstances.get(0);
    String processInstanceId = processInstance.getId();
    if (lockedWorkflowInstances.contains(processInstanceId)) {
      throw new RuntimeException("Process instance "+processInstanceId+" is already locked");
    }
    lockedWorkflowInstances.add(processInstanceId);
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(workflowEngineId);
    processInstance.setLock(lock);
    // log.debug("Locked process instance: "+jsonService.objectToJsonStringPretty(processInstance));
    return processInstance;
  }
  
  public boolean meetsConditions(WorkflowInstanceImpl processInstance, WorkflowInstanceQueryImpl processInstanceQuery) {
    if (processInstanceQuery.activityInstanceId!=null && !containsCompositeInstance(processInstance, processInstanceQuery.activityInstanceId)) {
      return false;
    }
    return true;
  }

  boolean containsCompositeInstance(ScopeInstanceImpl scopeInstance, Object activityInstanceId) {
    if (scopeInstance.hasActivityInstances()) {
      for (ActivityInstanceImpl activityInstance : scopeInstance.getActivityInstances()) {
        if (containsActivityInstance(activityInstance, activityInstanceId)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean containsActivityInstance(ActivityInstanceImpl activityInstance, Object activityInstanceId) {
    if (activityInstanceId.equals(activityInstance.getId())) {
      return true;
    }
    return containsCompositeInstance(activityInstance, activityInstanceId);
  }
  
  @Override
  public void deleteWorkflowInstance(String workflowInstanceId) {
    workflowInstances.remove(workflowInstanceId);
  }
}

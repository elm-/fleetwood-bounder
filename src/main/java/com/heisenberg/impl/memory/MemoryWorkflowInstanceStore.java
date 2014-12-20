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

import org.glassfish.jersey.client.Initializable;

import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.WorkflowInstanceQueryImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.WorkflowInstanceStore;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.impl.util.Lists;


/**
 * @author Walter White
 */
public class MemoryWorkflowInstanceStore implements WorkflowInstanceStore {

  protected String processEngineId;
  protected Map<Object, WorkflowInstanceImpl> processInstances;
  protected Set<Object> lockedProcessInstances;
  
  public MemoryWorkflowInstanceStore() {
  }
  
  public MemoryWorkflowInstanceStore(ServiceRegistry serviceRegistry) {
    this.processInstances = new ConcurrentHashMap<>();
    this.lockedProcessInstances = Collections.synchronizedSet(new HashSet<>());
    this.processEngineId = serviceRegistry.getService(WorkflowEngineImpl.class).getId();
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
    processInstances.put(processInstance.getId(), processInstance);
  }

  @Override
  public void flush(WorkflowInstanceImpl processInstance) {
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl processInstance) {
    lockedProcessInstances.remove(processInstance.getId());
    processInstance.removeLock();
  }
  
  @Override
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQueryImpl processInstanceQuery) {
    if (processInstanceQuery.processInstanceId!=null) {
      return Lists.of(processInstances.get(processInstanceQuery.processInstanceId));
    }
    List<WorkflowInstanceImpl> processInstances = new ArrayList<>();
    for (WorkflowInstanceImpl processInstance: this.processInstances.values()) {
      if (meetsConditions(processInstance, processInstanceQuery)) {
        processInstances.add(processInstance);
      }
    }
    return processInstances;
  }

  @Override
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceQueryImpl processInstanceQuery) {
    processInstanceQuery.setMaxResults(1);
    List<WorkflowInstanceImpl> processInstances = findWorkflowInstances(processInstanceQuery);
    if (processInstances==null || processInstances.isEmpty()) { 
      throw new RuntimeException("Process instance doesn't exist");
    }
    WorkflowInstanceImpl processInstance = processInstances.get(0);
    String processInstanceId = processInstance.getId();
    if (lockedProcessInstances.contains(processInstanceId)) {
      throw new RuntimeException("Process instance "+processInstanceId+" is already locked");
    }
    lockedProcessInstances.add(processInstanceId);
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(processEngineId);
    processInstance.setLock(lock);
    // log.debug("Locked process instance: "+jsonService.objectToJsonStringPretty(processInstance));
    return processInstance;
  }
  
  @Override
  public WorkflowInstanceImpl findWorkflowInstanceById(String processInstanceId) {
    return processInstances.get(processInstanceId);
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
}
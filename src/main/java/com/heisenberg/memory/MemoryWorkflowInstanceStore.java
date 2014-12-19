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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.jersey.client.Initializable;

import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQueryImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.WorkflowInstanceStore;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.util.Lists;
import com.heisenberg.plugin.ServiceRegistry;


/**
 * @author Walter White
 */
public class MemoryWorkflowInstanceStore implements WorkflowInstanceStore {

  protected String processEngineId;
  protected Map<Object, ProcessInstanceImpl> processInstances;
  protected Set<Object> lockedProcessInstances;
  
  public MemoryWorkflowInstanceStore() {
  }
  
  public MemoryWorkflowInstanceStore(ServiceRegistry serviceRegistry) {
    this.processInstances = new ConcurrentHashMap<>();
    this.lockedProcessInstances = Collections.synchronizedSet(new HashSet<>());
    this.processEngineId = serviceRegistry.getService(ProcessEngineImpl.class).getId();
  }

  @Override
  public String createProcessInstanceId(ProcessDefinitionImpl processDefinition) {
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
  public void insertProcessInstance(ProcessInstanceImpl processInstance) {
    processInstances.put(processInstance.getId(), processInstance);
//    log.debug("Saving: "+jsonService.objectToJsonStringPretty(processInstance));
  }

  @Override
  public void flush(ProcessInstanceImpl processInstance) {
//    List<Update> updates = processInstance.getUpdates();
//    if (updates!=null) {
//      log.debug("Flushing updates: ");
//      for (Update update : updates) {
//        log.debug("  " + jsonService.objectToJsonString(update));
//      }
//    } else {
//      log.debug("No updates to flush");
//    }
//    processInstance.setUpdates(new ArrayList<Update>());
  }

  @Override
  public void flushAndUnlock(ProcessInstanceImpl processInstance) {
    lockedProcessInstances.remove(processInstance.getId());
    processInstance.removeLock();
//    flush(processInstance);
//    log.debug("Process instance should be: "+jsonService.objectToJsonStringPretty(processInstance));
  }
  
  @Override
  public List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQueryImpl processInstanceQuery) {
    if (processInstanceQuery.processInstanceId!=null) {
      return Lists.of(processInstances.get(processInstanceQuery.processInstanceId));
    }
    List<ProcessInstanceImpl> processInstances = new ArrayList<>();
    for (ProcessInstanceImpl processInstance: this.processInstances.values()) {
      if (meetsConditions(processInstance, processInstanceQuery)) {
        processInstances.add(processInstance);
      }
    }
    return processInstances;
  }

  @Override
  public ProcessInstanceImpl lockProcessInstance(ProcessInstanceQueryImpl processInstanceQuery) {
    processInstanceQuery.setMaxResults(1);
    List<ProcessInstanceImpl> processInstances = findProcessInstances(processInstanceQuery);
    if (processInstances==null || processInstances.isEmpty()) { 
      throw new RuntimeException("Process instance doesn't exist");
    }
    ProcessInstanceImpl processInstance = processInstances.get(0);
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
  public ProcessInstanceImpl findProcessInstanceById(String processInstanceId) {
    return processInstances.get(processInstanceId);
  }

  public boolean meetsConditions(ProcessInstanceImpl processInstance, ProcessInstanceQueryImpl processInstanceQuery) {
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

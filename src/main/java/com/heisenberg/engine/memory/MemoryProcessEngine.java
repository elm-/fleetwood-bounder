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
package com.heisenberg.engine.memory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.heisenberg.ProcessDefinitionQuery;
import com.heisenberg.ProcessInstanceQuery;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.engine.updates.Update;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.Services;
import com.heisenberg.instance.ActivityInstanceId;
import com.heisenberg.instance.LockImpl;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.instance.ProcessInstanceId;
import com.heisenberg.json.Json;
import com.heisenberg.util.Lists;
import com.heisenberg.util.Time;


/** In memory (synchronized map based) process engine.
 * 
 * This implementation leverages the default process engine implementation use of UUIDs so it can be clustered.
 * 
 * @author Walter White
 */
public class MemoryProcessEngine extends ProcessEngineImpl {
  
  protected Map<ProcessDefinitionId, ProcessDefinitionImpl> processDefinitions;
  protected Map<ProcessInstanceId, ProcessInstanceImpl> processInstances;
  protected Set<ProcessInstanceId> lockedProcessInstances;
  
  public MemoryProcessEngine() {
    processDefinitions = Collections.synchronizedMap(new HashMap<ProcessDefinitionId, ProcessDefinitionImpl>());
    processInstances = Collections.synchronizedMap(new HashMap<ProcessInstanceId, ProcessInstanceImpl>());
    lockedProcessInstances = Collections.synchronizedSet(new HashSet<ProcessInstanceId>());
    try {
      id = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      id = "amnesia";
    }
    executor = createDefaultExecutor();
    json = new Json();
    services = new Services();
  }

  @Override
  protected void storeProcessDefinition(ProcessDefinitionImpl processDefinition) {
    processDefinitions.put(processDefinition.getId(), processDefinition);
    processDefinition.prepare();
  }

  @Override
  public void saveProcessInstance(ProcessInstanceImpl processInstance) {
    processInstances.put(processInstance.getId(), processInstance);
    log.debug("Saving: "+json.toJsonStringPretty(processInstance));
  }

  @Override
  public void flush(ProcessInstanceImpl processInstance) {
    List<Update> updates = processInstance.getUpdates();
    if (updates!=null) {
      log.debug("Flushing updates: ");
      for (Update update : updates) {
        log.debug("  " + update.toString());
      }
    } else {
      log.debug("No updates to flush");
    }
  }

  @Override
  public void flushAndUnlock(ProcessInstanceImpl processInstance) {
    lockedProcessInstances.remove(processInstance.getId());
    processInstance.removeLock();
    flush(processInstance);
    log.debug("Process instance should be: "+json.toJsonStringPretty(processInstance));
  }

  @Override
  public List<ProcessDefinitionImpl> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery) {
    if (processDefinitionQuery.getProcessDefinitionId()!=null) {
      ProcessDefinitionImpl processDefinition = processDefinitions.get(processDefinitionQuery.getProcessDefinitionId());
      return Lists.of(processDefinition);
    }
    List<ProcessDefinitionImpl> result = new ArrayList<>();
    for (ProcessDefinitionImpl processDefinition: processDefinitions.values()) {
      if (processDefinitionQuery.satisfiesCriteria(processDefinition)) {
        result.add(processDefinition);
      }
    }
    return result;
  }
  
  @Override
  public List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQuery processInstanceQuery) {
    if (processInstanceQuery.getProcessInstanceId()!=null) {
      ProcessInstanceImpl processInstance = processInstances.get(processInstanceQuery.getProcessInstanceId());
      return Lists.of(processInstance);
    }
    List<ProcessInstanceImpl> result = new ArrayList<>();
    for (ProcessInstanceImpl processInstance: processInstances.values()) {
      if (processInstanceQuery.satisfiesCriteria(processInstance)) {
        result.add(processInstance);
      }
    }
    return result;
  }

  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId) {
    ProcessInstanceQuery processInstanceQuery = buildProcessInstanceQuery()
      .activityInstanceId(activityInstanceId)
      .getQuery();
    processInstanceQuery.setMaxResults(1);
    List<ProcessInstanceImpl> processInstances = findProcessInstances(processInstanceQuery);
    ProcessInstanceImpl processInstance = (!processInstances.isEmpty() ? processInstances.get(0) : null);
    if (processInstance==null) { 
      throw new RuntimeException("Process instance "+id+" doesn't exist");
    }
    ProcessInstanceId id = processInstance.getId();
    if (lockedProcessInstances.contains(id)) {
      throw new RuntimeException("Process instance "+id+" is already locked");
    }
    lockedProcessInstances.add(id);
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    processInstance.setLock(lock);
    log.debug("Locked process instance: "+json.toJsonStringPretty(processInstance));
    return processInstance;
  }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.heisenberg.api.ProcessDefinitionQuery;
import com.heisenberg.api.ProcessInstanceQuery;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.engine.updates.Update;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.ActivityInstanceId;
import com.heisenberg.instance.LockImpl;
import com.heisenberg.instance.ProcessInstanceId;
import com.heisenberg.instance.ProcessInstanceImpl;
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
  }

  @Override
  protected void storeProcessDefinition(ProcessDefinitionImpl processDefinition) {
    processDefinition.prepare();
    processDefinitions.put(processDefinition.id, processDefinition);
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
  protected ProcessDefinitionImpl loadProcessDefinitionById(ProcessDefinitionId processDefinitionId) {
    return processDefinitions.get(processDefinitionId);
  }

  @Override
  public List<ProcessDefinition> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery) {
    if (processDefinitionQuery.getProcessDefinitionId()!=null) {
      ProcessDefinitionImpl processDefinition = processDefinitions.get(processDefinitionQuery.getProcessDefinitionId());
      throw new RuntimeException("TODO");
    }
    List<ProcessDefinitionImpl> result = new ArrayList<>();
    for (ProcessDefinitionImpl processDefinition: processDefinitions.values()) {
      if (processDefinitionQuery.satisfiesCriteria(processDefinition)) {
        result.add(processDefinition);
      }
    }
    throw new RuntimeException("TODO");
  }
  
  public List<ProcessInstance> findProcessInstances(ProcessInstanceQuery processInstanceQuery) {
    throw new RuntimeException("TODO");
  }
  
  public ProcessInstanceImpl findProcessInstance(ProcessInstanceQuery processInstanceQuery) {
    if (processInstanceQuery.getProcessInstanceId()!=null) {
      ProcessInstanceImpl processInstance = processInstances.get(processInstanceQuery.getProcessInstanceId());
      return processInstance;
    }
    for (ProcessInstanceImpl processInstance: processInstances.values()) {
      if (processInstanceQuery.satisfiesCriteria(processInstance)) {
        return processInstance;
      }
    }
    return null;
  }

  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId) {
    ProcessInstanceQuery processInstanceQuery = buildProcessInstanceQuery()
      .activityInstanceId(activityInstanceId)
      .getQuery();
    processInstanceQuery.setMaxResults(1);
    ProcessInstanceImpl processInstance = findProcessInstance(processInstanceQuery);
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

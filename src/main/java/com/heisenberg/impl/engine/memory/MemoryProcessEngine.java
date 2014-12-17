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
package com.heisenberg.impl.engine.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.heisenberg.api.MemoryProcessEngineConfiguration;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.impl.PageImpl;
import com.heisenberg.impl.ProcessDefinitionQueryImpl;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQueryImpl;
import com.heisenberg.impl.ProcessInstanceQueryImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.engine.updates.Update;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.util.Lists;


/** In memory (synchronized map based) process engine.
 * 
 * This implementation leverages the default process engine implementation use of UUIDs so it can be clustered.
 * 
 * @author Walter White
 */
public class MemoryProcessEngine extends ProcessEngineImpl {
  
  protected Map<Object, ProcessDefinitionImpl> processDefinitions;
  protected Map<Object, ProcessInstanceImpl> processInstances;
  protected Set<Object> lockedProcessInstances;
  
  public MemoryProcessEngine() {
    this(new MemoryProcessEngineConfiguration());
  }
  
  public MemoryProcessEngine(MemoryProcessEngineConfiguration configuration) {
    super(configuration);
    processDefinitions = Collections.synchronizedMap(new HashMap<Object, ProcessDefinitionImpl>());
    processInstances = Collections.synchronizedMap(new HashMap<Object, ProcessInstanceImpl>());
    lockedProcessInstances = Collections.synchronizedSet(new HashSet<Object>());
  }

  @Override
  public MemoryTaskService getTaskService() {
    return (MemoryTaskService) taskService;
  }

  @Override
  public void insertProcessDefinition(ProcessDefinitionImpl processDefinition) {
    processDefinitions.put(processDefinition.id, processDefinition);
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
  
  public List<ProcessDefinitionImpl> loadProcessDefinitions(ProcessDefinitionQueryImpl query) {
    List<ProcessDefinitionImpl> result = null;
    if (query.id!=null) {
      result = new ArrayList<ProcessDefinitionImpl>();
      ProcessDefinitionImpl processDefinition = processDefinitions.get(query.id);
      if (processDefinition!=null) {
        result.add(processDefinition);
      }
    } else if (result==null) {
      result = new ArrayList<ProcessDefinitionImpl>(processDefinitions.values());
    }
    if (query.name!=null && !result.isEmpty()) {
      filterByName(result, query.name);
    }
    if (query.maxResults!=null) {
      while (result.size()>query.maxResults) {
        result.remove(result.size()-1);
      }
    }
    return result;
  }
  
  private void filterByName(List<ProcessDefinitionImpl> result, String name) {
    for (int i=result.size()-1; i<=0; i--) {
      if (!name.equals(result.get(i).name)) {
        result.remove(i);
      }
    }
  }

  public boolean matchesProcessDefinitionCriteria(ProcessDefinitionImpl process, ProcessDefinitionQueryImpl query) {
    if (query.name!=null && !query.name.equals(process.name)) {
      return false;
    }
    return true;
  }

  public ProcessInstanceImpl findProcessInstance(ProcessInstanceQueryImpl processInstanceQuery) {
    if (processInstanceQuery.getProcessInstanceId()!=null) {
      ProcessInstanceImpl processInstance = processInstances.get(processInstanceQuery.getProcessInstanceId());
      return processInstance;
    }
    for (ProcessInstanceImpl processInstance: processInstances.values()) {
      if (meetsConditions(processInstance, processInstanceQuery)) {
        return processInstance;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQueryImpl activityInstanceQuery) {
    if (activityInstanceQuery.processInstanceId!=null) {
      return Lists.of(processInstances.get(activityInstanceQuery.processInstanceId));
    }
    return Collections.EMPTY_LIST;
  }

//  void scanActivityInstances(ScopeInstanceImpl scopeInstance, ProcessInstanceQueryImpl activityInstanceQuery, List<ActivityInstance> activityInstan) {
//    if (scopeInstance.hasActivityInstances()) {
//      for (ActivityInstanceImpl activityInstance: scopeInstance.activityInstances) {
//        if (activityInstanceQuery.meetsConditions(activityInstance)) {
//          page.add(activityInstance);
//        }
//        scanActivityInstances(activityInstance, activityInstanceQuery, page);
//      }
//    }
//  }

  @Override
  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(String processInstanceId, String activityInstanceId) {
    ProcessInstanceQueryImpl processInstanceQueryImpl = new ProcessInstanceQueryImpl(this)
      .processInstanceId(processInstanceId)
      .activityInstanceId(activityInstanceId);
    processInstanceQueryImpl.setMaxResults(1);
    ProcessInstanceImpl processInstance = findProcessInstance(processInstanceQueryImpl);
    if (processInstance==null) { 
      throw new RuntimeException("Process instance "+id+" doesn't exist");
    }
    Object id = processInstance.getId();
    if (lockedProcessInstances.contains(id)) {
      throw new RuntimeException("Process instance "+id+" is already locked");
    }
    lockedProcessInstances.add(id);
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());
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

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.engine.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fleetwood.bounder.ProcessDefinitionQuery;
import fleetwood.bounder.ProcessInstanceQuery;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.Lock;
import fleetwood.bounder.instance.ProcessEngineImpl;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;
import fleetwood.bounder.json.JacksonJsonSerializer;
import fleetwood.bounder.util.Lists;
import fleetwood.bounder.util.Time;


/** In memory (synchronized map based) process engine.
 * 
 * This implementation leverages the default process engine implementation use of UUIDs so it can be clustered.
 * 
 * @author Walter White
 */
public class MemoryProcessEngine extends ProcessEngineImpl {
  
  protected Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = Collections.synchronizedMap(new HashMap<ProcessDefinitionId, ProcessDefinition>());
  protected Map<ProcessInstanceId, ProcessInstance> processInstances = Collections.synchronizedMap(new HashMap<ProcessInstanceId, ProcessInstance>());
  protected Set<ProcessInstanceId> lockedProcessInstances = Collections.synchronizedSet(new HashSet<ProcessInstanceId>());

  @Override
  protected void storeProcessDefinition(ProcessDefinition processDefinition) {
    processDefinitions.put(processDefinition.getId(), processDefinition);
    processDefinition.setProcessEngine(this);
    processDefinition.prepare();
  }

  @Override
  public void saveProcessInstance(ProcessInstance processInstance) {
    processInstances.put(processInstance.getId(), processInstance);
    log.debug("Saving: "+JacksonJsonSerializer.toJsonStringPretty(processInstance));
  }

  @Override
  public void flush(ProcessInstance processInstance) {
    List<Update> updates = processInstance.getUpdates();
    if (updates!=null) {
      log.debug("Flushing updates: ");
      for (Update update : updates) {
        log.debug("  " + JacksonJsonSerializer.toJsonString(update));
      }
    } else {
      log.debug("No updates to flush");
    }
  }

  @Override
  public void flushAndUnlock(ProcessInstance processInstance) {
    lockedProcessInstances.remove(processInstance.getId());
    processInstance.removeLock();
    flush(processInstance);
    log.debug("Process instance should be: "+JacksonJsonSerializer.toJsonStringPretty(processInstance));
  }

  @Override
  public List<ProcessDefinition> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery) {
    if (processDefinitionQuery.getProcessDefinitionId()!=null) {
      ProcessDefinition processDefinition = processDefinitions.get(processDefinitionQuery.getProcessDefinitionId());
      return Lists.of(processDefinition);
    }
    List<ProcessDefinition> result = new ArrayList<>();
    for (ProcessDefinition processDefinition: processDefinitions.values()) {
      if (processDefinitionQuery.satisfiesCriteria(processDefinition)) {
        result.add(processDefinition);
      }
    }
    return result;
  }
  
  @Override
  public List<ProcessInstance> findProcessInstances(ProcessInstanceQuery processInstanceQuery) {
    if (processInstanceQuery.getProcessInstanceId()!=null) {
      ProcessInstance processInstance = processInstances.get(processInstanceQuery.getProcessInstanceId());
      return Lists.of(processInstance);
    }
    List<ProcessInstance> result = new ArrayList<>();
    for (ProcessInstance processInstance: processInstances.values()) {
      if (processInstanceQuery.satisfiesCriteria(processInstance)) {
        result.add(processInstance);
      }
    }
    return result;
  }

  public ProcessInstance lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId) {
    ProcessInstanceQuery processInstanceQuery = buildProcessInstanceQuery()
      .activityInstanceId(activityInstanceId)
      .getQuery();
    processInstanceQuery.setMaxResults(1);
    List<ProcessInstance> processInstances = findProcessInstances(processInstanceQuery);
    ProcessInstance processInstance = (!processInstances.isEmpty() ? processInstances.get(0) : null);
    if (processInstance==null) { 
      throw new RuntimeException("Process instance "+id+" doesn't exist");
    }
    ProcessInstanceId id = processInstance.getId();
    if (lockedProcessInstances.contains(id)) {
      throw new RuntimeException("Process instance "+id+" is already locked");
    }
    lockedProcessInstances.add(id);
    Lock lock = new Lock();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    processInstance.setLock(lock);
    log.debug("Locked process instance: "+JacksonJsonSerializer.toJsonStringPretty(processInstance));
    return processInstance;
  }
}

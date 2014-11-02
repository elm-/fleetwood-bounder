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

package fleetwood.bounder.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.engine.ProcessEngineImpl;
import fleetwood.bounder.engine.updates.ActivityInstanceStart;
import fleetwood.bounder.engine.updates.LockRemove;
import fleetwood.bounder.engine.updates.ToStartAdd;
import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.util.Time;




/**
 * @author Walter White
 */
public class ProcessInstance extends CompositeInstance {
  
  protected ProcessInstanceId id;
  protected Lock lock;

  @JsonIgnore
  protected List<Update> updates;
  @JsonIgnore
  protected Queue<ActivityInstance> toStart;
  @JsonIgnore
  protected Queue<ActivityInstance> async;
  
  public void start() {
    ProcessEngineImpl.log.debug("Starting "+this);
    this.start = Time.now();
    List<ActivityDefinition> startActivityDefinitions = compositeDefinition.getStartActivityDefinitions();
    if (startActivityDefinitions!=null) {
      for (ActivityDefinition startActivityDefinition: startActivityDefinitions) {
        ActivityInstance activityInstance = createActivityInstance(startActivityDefinition);
        processInstance.startActivityInstance(activityInstance);
      }
    }
    lock = new Lock();
    lock.setTime(Time.now());
    lock.setOwner(processEngine.getId());
    save();
    executeOperations();
  }
  
  void startActivityInstance(ActivityInstance activityInstance) {
    if (toStart==null) {
      toStart = new LinkedList<>();
    }
    toStart.add(activityInstance);
    addUpdate(new ToStartAdd(processEngine, activityInstance));
  }
  
  protected void addUpdate(Update update) {
    // we only must capture the updates after the first save
    // so the collection is initialized after the save by the process store
    if (updates!=null) {
      updates.add(update);
    }
  }

  void executeOperations() {
    if (toStart!=null) {
      while (!toStart.isEmpty()) {
        processEngine.flushUpdates(this);
        updates = new ArrayList<>();
        ActivityInstance activityInstance = toStart.remove();
        activityInstance.setStart(Time.now());
        addUpdate(new ActivityInstanceStart(processEngine, activityInstance));
        ActivityDefinition activityDefinition = activityInstance.getActivityDefinition();
        ProcessEngineImpl.log.debug("Starting "+activityInstance);
        activityDefinition.start(activityInstance);
      }
    }
    processEngine.flushUpdatesAndUnlock(processInstance);
  }

  public ProcessInstanceId getId() {
    return id;
  }

  public void setId(ProcessInstanceId id) {
    this.id = id;
  }

  public String toString() {
    return "("+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+")";
  }

  public void save() {
    processEngine.saveProcessInstance(processInstance);
    updates = new ArrayList<>();
  }

  public String toJson() {
    return processEngine.getJson().toJsonString(this);
  }
  
  public List<Update> getUpdates() {
    return updates;
  }

  public void setUpdates(List<Update> updates) {
    this.updates = updates;
  }

  public void removeLock() {
    setLock(null);
  }

  public Lock getLock() {
    return lock;
  }

  public void setLock(Lock lock) {
    if (this.lock!=null && lock==null) {
      addUpdate(new LockRemove(processEngine));
    }
    this.lock = lock;
  }
  
  @JsonAnyGetter
  public Map<String , Object> getOtherFields() {
    if ( (toStart==null || toStart.isEmpty())
         && (async==null || toStart.isEmpty())
       ) {
      return null;
    }
    Map<String, Object> otherFields = new HashMap<>();
    if (toStart!=null && !toStart.isEmpty()) {
      List<ActivityInstanceId> toStartIds = new ArrayList<>();
      for (ActivityInstance activityInstance: toStart) {
        toStartIds.add(activityInstance.id);
      }
      otherFields.put("toStart", toStartIds);
    }
    return otherFields;
  }
}

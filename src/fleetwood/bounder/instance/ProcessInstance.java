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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.engine.updates.LockReleaseUpdate;
import fleetwood.bounder.engine.updates.OperationAddUpdate;
import fleetwood.bounder.engine.updates.OperationRemoveUpdate;
import fleetwood.bounder.engine.updates.Update;




/**
 * @author Walter White
 */
public class ProcessInstance extends CompositeInstance {
  
  protected ProcessInstanceId id;
  protected Lock lock;

  @JsonIgnore
  protected List<Update> updates;
  @JsonIgnore
  protected Queue<Operation> operations;
  
  public ProcessInstance(ProcessEngineImpl processEngine, ProcessDefinition processDefinition, ProcessInstanceId processInstanceId) {
    setId(processInstanceId);
    setProcessEngine(processEngine);
    setProcessDefinition(processDefinition);
    setCompositeDefinition(processDefinition);
    setProcessInstance(this);
    ProcessEngineImpl.log.debug("Created "+processInstance);
  }

  // Adds the activity instances to the list of activity instances to start.
  // This method assumes that executeOperations was already called or will be called. 
  void startActivityInstance(ActivityInstance activityInstance) {
    addOperation(new ActivityInstanceStartOperation(activityInstance));
  }
  
  void addOperation(Operation operation) {
    if (operations==null) {
      operations = new LinkedList<>();
    }
    operations.add(operation);
    addUpdate(new OperationAddUpdate(operation));
  }
  
  Operation removeOperation() {
    Operation operation = operations!=null ? operations.poll() : null;
    if (operation!=null) {
      addUpdate(new OperationRemoveUpdate(operation));
    }
    return operation; 
  }

  void addUpdate(Update update) {
    // we only must capture the updates after the first save
    // so the collection is initialized after the save by the process store
    if (updates!=null) {
      updates.add(update);
    }
  }

  // to be called from the process engine
  void executeOperations() {
    updates = new ArrayList<Update>();
    while (hasOperations()) {
      // in the first iteration, the updates will be empty and hence no updates will be flushed
      flushUpdates(); // first time round, the 
      Operation operation = removeOperation();
      operation.execute();
    }
    processEngine.flushAndUnlock(processInstance);
  }
  
  boolean hasOperations() {
    return operations!=null && !operations.isEmpty();
  }

  void flushUpdates() {
    if (!updates.isEmpty()) {
      processEngine.flushUpdates(this);
      updates = new ArrayList<>();
    }
  }

  public ProcessInstanceId getId() {
    return id;
  }

  public void setId(ProcessInstanceId id) {
    this.id = id;
  }

  public String toString() {
    return "pi("+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+")";
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
      addUpdate(new LockReleaseUpdate());
    }
    if (this.lock==null && lock!=null) {
      addUpdate(new LockAcquireUpdate(lock));
    }
    this.lock = lock;
  }
  
  public Queue<Operation> getOperations() {
    return operations;
  }

  
  public void setOperations(Queue<Operation> operations) {
    this.operations = operations;
  }
  
//  @JsonAnyGetter
//  public Map<String , Object> getOtherFields() {
//    if ( (operations==null || operations.isEmpty())
//         && (async==null || operations.isEmpty())
//       ) {
//      return null;
//    }
//    Map<String, Object> otherFields = new HashMap<>();
//    if (operations!=null && !operations.isEmpty()) {
//      List<ActivityInstanceId> toStartIds = new ArrayList<>();
//      for (ActivityInstance activityInstance: operations) {
//        toStartIds.add(activityInstance.id);
//      }
//      otherFields.put("toStart", toStartIds);
//    }
//    return otherFields;
//  }
}

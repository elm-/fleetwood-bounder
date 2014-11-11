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
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.engine.operation.ActivityInstanceStartOperation;
import fleetwood.bounder.engine.operation.Operation;
import fleetwood.bounder.engine.updates.AsyncOperationAddUpdate;
import fleetwood.bounder.engine.updates.LockAcquireUpdate;
import fleetwood.bounder.engine.updates.LockReleaseUpdate;
import fleetwood.bounder.engine.updates.OperationAddUpdate;
import fleetwood.bounder.engine.updates.OperationRemoveUpdate;
import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.json.JsonWriter;
import fleetwood.bounder.util.Time;




/**
 * @author Walter White
 */
public class ProcessInstance extends ScopeInstance {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public static final String FIELD_ID = "id";
  protected ProcessInstanceId id;

  public static final String FIELD_LOCK = "lock";
  protected Lock lock;

  public static final String FIELD_OPERATIONS = "operations";
  protected Queue<Operation> operations;

  public static final String FIELD_ASYNC_OPERATIONS = "asyncOperations";
  protected Queue<Operation> asyncOperations;
  
  protected List<Update> updates;
  protected boolean isAsync;
  
  public ProcessInstance(ProcessEngineImpl processEngine, ProcessDefinition processDefinition, ProcessInstanceId processInstanceId) {
    setId(processInstanceId);
    setProcessEngine(processEngine);
    setProcessDefinition(processDefinition);
    setScopeDefinition(processDefinition);
    setProcessInstance(this);
    setStart(Time.now());
    initializeVariableInstances();
    log.debug("Created "+processInstance);
  }

  // Adds the activity instances to the list of activity instances to start.
  // This method assumes that executeOperations was already called or will be called. 
  void startActivityInstance(ActivityInstance activityInstance) {
    addOperation(new ActivityInstanceStartOperation(activityInstance));
  }
  
  void addOperation(Operation operation) {
    if (isAsync || !operation.isAsync()) {
      if (operations==null) {
        operations = new LinkedList<>();
      }
      operations.add(operation);
      addUpdate(new OperationAddUpdate(operation));
    } else {
      if (asyncOperations==null) {
        asyncOperations = new LinkedList<>();
      }
      asyncOperations.add(operation);
      addUpdate(new AsyncOperationAddUpdate(operation));
    }
  }
  
  Operation removeOperation() {
    Operation operation = operations!=null ? operations.poll() : null;
    if (operation!=null) {
      addUpdate(new OperationRemoveUpdate(operation));
    }
    return operation; 
  }

  public void addUpdate(Update update) {
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
      operation.execute(processEngine);
    }
    if (hasAsyncWork()) {
      processEngine.flush(processInstance);
      Executor executor = processEngine.getExecutor();
      executor.execute(new Runnable(){
        public void run() {
          operations = asyncOperations;
          asyncOperations = null;
          executeOperations();
        }});
    } else {
      processEngine.flushAndUnlock(processInstance);
    }
  }
  
  boolean hasAsyncWork() {
    return asyncOperations!=null && !asyncOperations.isEmpty();
  }

  boolean hasOperations() {
    return operations!=null && !operations.isEmpty();
  }

  void flushUpdates() {
    if (!updates.isEmpty()) {
      processEngine.flush(this);
      updates = new ArrayList<>();
    }
  }
  
  public void end() {
    if (this.end==null) {
      if (hasUnfinishedActivityInstances()) {
        throw new RuntimeException("Can't end this process instance. There are unfinished activity instances: "+this);
      }
      setEnd(Time.now());
      
      // Each operation is an extra flush.  So this if limits the number of flushes
      // if (thereProcessInstanceCouldTriggerNotifications) {
      //   processInstance.addOperation(new NotifyProcessInstanceEnded(this));
      // }
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
  
  public boolean isAsync() {
    return isAsync;
  }

  public void setAsync(boolean isAsync) {
    this.isAsync = isAsync;
  }

  public Queue<Operation> getAsyncOperations() {
    return asyncOperations;
  }

  public void setAsyncOperations(Queue<Operation> asyncOperations) {
    this.asyncOperations = asyncOperations;
  }
  
  public void setEnd(Long end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = end-start;
    }
    // when we add call activity we will need:
    // addUpdate(new ProcessInstanceEndUpdate(this));
  }

  public void write(JsonWriter writer) {
    writer.writeObjectStart(this);
    writer.writeIdField(FIELD_ID, id);
    serializeCompositeInstanceFields(writer);
    writer.writeObjectArray(FIELD_OPERATIONS, operations);
    writer.writeObjectArray(FIELD_ASYNC_OPERATIONS, asyncOperations);
    writer.writeObject(FIELD_LOCK, lock);
    writer.writeObjectEnd(this);
  }
}

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

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.engine.Operation;
import fleetwood.bounder.engine.StartActivityInstance;
import fleetwood.bounder.store.updates.AddOperation;
import fleetwood.bounder.store.updates.RemoveOperation;




/**
 * @author Walter White
 */
public class ProcessInstance extends CompositeInstance {
  
  protected ProcessInstanceId id;
  @JsonIgnore
  protected List<Update> updates;
  protected Queue<Operation> operations;
  
  @Override
  public void start() {
    ProcessEngine.log.debug("Starting "+this);
    super.start();
    save();
    executeOperations();
  }
  
  void startActivityInstance(ActivityInstance activityInstance) {
    addOperation(new StartActivityInstance(activityInstance));
  }

  void addOperation(Operation operation) {
    if (operations==null) {
      operations = new LinkedList<>();
    }
    operations.add(operation);
    addUpdate(new AddOperation(operation));
  }
  
  protected void addUpdate(Update update) {
    // we only must capture the updates after the first save
    // so the collection is initialized after the save by the process store
    if (updates!=null) {
      updates.add(update);
    }
  }

  void executeOperations() {
    if (operations!=null) {
      while (!operations.isEmpty()) {
        processStore.flushUpdates(this);
        updates = new ArrayList<>();
        Operation operation = operations.remove();
        addUpdate(new RemoveOperation(operation));
        operation.execute();
      }
    }
    processStore.flushUpdatesAndUnlock(processInstance);
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
    processStore.saveProcessInstance(processInstance);
    updates = new ArrayList<>();
  }

  public String toJson() {
    return processStore.getProcessEngine().getJson().toJsonString(this);
  }
  
  public List<Update> getUpdates() {
    return updates;
  }

  public void setUpdates(List<Update> updates) {
    this.updates = updates;
  }

  public Queue<Operation> getOperations() {
    return operations;
  }
  
  public void setOperations(Queue<Operation> operations) {
    this.operations = operations;
  }
}

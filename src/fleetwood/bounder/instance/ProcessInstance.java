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

import java.util.LinkedList;
import java.util.Queue;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.engine.Operation;
import fleetwood.bounder.engine.StartActivityInstance;




/**
 * @author Tom Baeyens
 */
public class ProcessInstance extends CompositeInstance {
  
  protected ProcessInstanceId id;
  protected ProcessInstanceUpdates processInstanceUpdates;
  
  protected Queue<Operation> operations;
  
  @Override
  public void start() {
    ProcessEngine.log.debug("Starting "+this);
    super.start();
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
  }

  void executeOperations() {
    if (operations!=null) {
      while (!operations.isEmpty()) {
        processStore.flushUpdates(processInstanceUpdates);
        operations.remove().execute();
      }
    }
    processStore.flushUpdatesAndUnlock(processInstanceUpdates);
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
  }
}

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
package com.heisenberg.impl.instance;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.activities.bpmn.CallActivity;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.impl.ExecutorService;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQueryImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.engine.operation.Operation;
import com.heisenberg.impl.engine.updates.LockAcquireUpdate;
import com.heisenberg.impl.engine.updates.LockReleaseUpdate;
import com.heisenberg.impl.engine.updates.OperationAddUpdate;
import com.heisenberg.impl.engine.updates.OperationRemoveFirstUpdate;
import com.heisenberg.impl.engine.updates.Update;




/**
 * @author Walter White
 */
@JsonPropertyOrder({"id", "processDefinitionId", "start", "end", "duration", "activityInstances", "variableInstances"})
public class ProcessInstanceImpl extends ScopeInstanceImpl implements ProcessInstance {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public String processDefinitionId;
  public LockImpl lock;
  public Queue<Operation> operations;
  public Queue<Operation> asyncOperations;
  public String organizationId;
  public String callerProcessInstanceId;
  public String callerActivityInstanceId;
  
  // As long as the process instance is not saved, the updates collection is null.
  // That means it's not yet necessary to collect the updates. 
  public List<Update> updates;
  
  @JsonIgnore
  public Boolean isAsync;
  
  @JsonIgnore
  public Map<String, Object> transientContext;

  public ProcessInstanceImpl() {
  }
  
  public ProcessInstanceImpl(ProcessEngineImpl processEngine, ProcessDefinitionImpl processDefinition, String processInstanceId) {
    setId(processInstanceId);
    setProcessEngine(processEngine);
    setOrganizationId(processDefinition.organizationId);
    setProcessDefinition(processDefinition);
    setProcessDefinitionId(processDefinition.id);
    setScopeDefinition(processDefinition);
    setProcessInstance(this);
    setStart(Time.now());
    initializeVariableInstances();
    log.debug("Created "+processInstance);
  }
  
  public void addOperation(Operation operation) {
    OperationAddUpdate update = operation.createUpdate();
    if (Boolean.TRUE.equals(isAsync) || !operation.isAsync()) {
      if (operations==null) {
        operations = new LinkedList<>();
      }
      operations.add(operation);
      addUpdate(update);
    } else {
      if (asyncOperations==null) {
        asyncOperations = new LinkedList<>();
      }
      asyncOperations.add(operation);
      update.isAsync = true;
      addUpdate(update);
    }
  }
  
  Operation removeOperation() {
    Operation operation = operations!=null ? operations.poll() : null;
    if (operation!=null) {
      addUpdate(new OperationRemoveFirstUpdate());
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
  public void executeOperations() {
    while (hasOperations()) {
      // in the first iteration, the updates will be empty and hence no updates will be flushed
      flushUpdates(); // first time round, the 
      Operation operation = removeOperation();
      operation.execute(processEngine);
    }
    if (hasAsyncWork()) {
      processEngine.flush(processInstance);
      ExecutorService executor = processEngine.getExecutorService();
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
    processEngine.flush(this);
  }
  
  @Override
  public void ended(ActivityInstanceImpl activityInstance) {
    if (!hasOpenActivityInstances()) {
      end();
    }
  }
  
  public void end() {
    if (this.end==null) {
      if (hasOpenActivityInstances()) {
        throw new RuntimeException("Can't end this process instance. There are open activity instances: "+this);
      }
      setEnd(Time.now());
      
      if (callerProcessInstanceId!=null) {
        ProcessInstanceQueryImpl processInstanceQuery = processEngine.newProcessInstanceQuery()
         .processInstanceId(callerProcessInstanceId)
         .activityInstanceId(callerActivityInstanceId);
        ProcessInstanceImpl callerProcessInstance = processEngine.lockProcessInstanceWithRetry(processInstanceQuery);
        ActivityInstanceImpl callerActivityInstance = callerProcessInstance.findActivityInstance(callerActivityInstanceId);
        if (callerActivityInstance.isEnded()) {
          throw new RuntimeException("Call activity instance "+callerActivityInstance+" is already ended");
        }
        log.debug("Call activity "+callerActivityInstance+" ends");
        ActivityDefinitionImpl activityDefinition = callerActivityInstance.getActivityDefinition();
        CallActivity callActivity = (CallActivity) activityDefinition.activityType;
        callActivity.calledProcessInstanceEnded(callerActivityInstance, this);
        callerActivityInstance.onwards();
        callerProcessInstance.executeOperations();
      }
      // Each operation is an extra flush.  So this if limits the number of flushes
      // if (thisProcessInstanceCouldTriggerNotifications) {
      //   processInstance.addOperation(new NotifyProcessInstanceEnded(this));
      // }
    }
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

  public LockImpl getLock() {
    return lock;
  }

  public void setLock(LockImpl lock) {
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
  
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setEnd(LocalDateTime end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = new Duration(start.toDateTime(), end.toDateTime()).getMillis();
    }
    // when we add call activity we will need:
    // addUpdate(new ProcessInstanceEndUpdate(this));
  }
  
  public Object getTransientContextObject(String key) {
    return transientContext!=null ? transientContext.get(key) : null;
  }
  
  public void visit(ProcessInstanceVisitor visitor) {
    visitor.startProcessInstance(this);
    visitLock(visitor);
    visitOperations(visitor);
    visitAsyncOperations(visitor);
    visitUpdates(visitor);
    visitCompositeInstance(visitor);
    visitor.endProcessInstance(this);
  }

  protected void visitLock(ProcessInstanceVisitor visitor) {
    if (lock!=null) {
      visitor.lock(lock);
    }
  }

  protected void visitOperations(ProcessInstanceVisitor visitor) {
    if (operations!=null) {
      int i=0;
      Iterator<Operation> iter = operations.iterator();
      while (iter.hasNext()) {
        Operation operation = iter.next();
        visitor.operation(operation, i);
        i++;
      }
    }
  }

  protected void visitAsyncOperations(ProcessInstanceVisitor visitor) {
    if (asyncOperations!=null) {
      int i=0;
      Iterator<Operation> iter = asyncOperations.iterator();
      while (iter.hasNext()) {
        Operation operation = iter.next();
        visitor.asyncOperation(operation, i);
        i++;
      }
    }
  }

  protected void visitUpdates(ProcessInstanceVisitor visitor) {
    if (updates!=null) {
      for (int i =0; i<updates.size(); i++) {
        Update update = updates.get(i);
        visitor.update(update, i);
      }
    }
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @Override
  public boolean isProcessInstance() {
    return true;
  }

  public String getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
}

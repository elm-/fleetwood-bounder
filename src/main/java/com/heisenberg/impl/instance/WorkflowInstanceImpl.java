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

import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_NOTIFYING;
import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING;
import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_CONTAINER;
import static com.heisenberg.impl.instance.ActivityInstanceImpl.STATE_STARTING_MULTI_INSTANCE;

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
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.activitytypes.CallActivity;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.ExecutorService;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.WorkflowInstanceQueryImpl;
import com.heisenberg.impl.WorkflowInstanceStore;
import com.heisenberg.impl.definition.ActivityImpl;
import com.heisenberg.impl.definition.WorkflowImpl;




/**
 * @author Walter White
 */
@JsonPropertyOrder({"id", "processDefinitionId", "start", "end", "duration", "activityInstances", "variableInstances"})
public class WorkflowInstanceImpl extends ScopeInstanceImpl implements WorkflowInstance {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

  public String workflowId;
  public LockImpl lock;
  public Queue<ActivityInstanceImpl> work;
  public Queue<ActivityInstanceImpl> workAsync;
  public String organizationId;
  public String callerWorkflowInstanceId;
  public String callerActivityInstanceId;
  
  @JsonIgnore
  public Boolean isAsync;
  
  @JsonIgnore
  public Map<String, Object> transientContext;

  public WorkflowInstanceImpl() {
  }
  
  public WorkflowInstanceImpl(WorkflowEngineImpl processEngine, WorkflowImpl processDefinition, String processInstanceId) {
    this.id = processInstanceId;
    this.workflowEngine = processEngine;
    this.organizationId = processDefinition.organizationId;
    this.workflow = processDefinition;
    this.workflowId = processDefinition.id;
    this.scopeDefinition = processDefinition;
    this.workflowInstance = this;
    this.start = Time.now();
    initializeVariableInstances();
    log.debug("Created "+workflowInstance);
  }
  
  protected void addWork(ActivityInstanceImpl activityInstance) {
    if (isWorkAsync(activityInstance)) {
      addAsyncWork(activityInstance);
    } else {
      addSyncWork(activityInstance);
    }
  }
  
  protected boolean isWorkAsync(ActivityInstanceImpl activityInstance) {
    // if this workflow instance is already running in an async thread, 
    // the new work should be done sync in this thread.
    if (Boolean.TRUE.equals(isAsync)) {
      return false;
    }
    if (!ActivityInstanceImpl.START_WORKSTATES.contains(activityInstance.workState)) {
      return false;
    }
    return activityInstance.getActivity().activityType.isAsync(activityInstance);
  }

  protected void addSyncWork(ActivityInstanceImpl activityInstance) {
    if (work==null) {
      work = new LinkedList<>();
    }
    work.add(activityInstance);
    if (updates!=null) {
      getUpdates().isWorkChanged = true;
    }
  }

  protected void addAsyncWork(ActivityInstanceImpl activityInstance) {
    if (workAsync==null) {
      workAsync = new LinkedList<>();
    }
    workAsync.add(activityInstance);
    if (updates!=null) {
      getUpdates().isAsyncWorkChanged = true;
    }
  }

  protected ActivityInstanceImpl getNextWork() {
    ActivityInstanceImpl nextWork = work!=null ? work.poll() : null;
    if (nextWork!=null && updates!=null) {
      getUpdates().isWorkChanged = true;
    }
    return nextWork;
  }

  // to be called from the process engine
  public void executeWork() {
    WorkflowInstanceStore workflowInstanceStore = workflowEngine.getWorkflowInstanceStore();
    boolean isFirst = true;
    while (hasWork()) {
      // in the first iteration, the updates will be empty and hence no updates will be flushed
      if (isFirst) {
        isFirst = false;
      } else {
        flushUpdates(); 
      }
      ActivityInstanceImpl activityInstance = getNextWork();
      ActivityImpl activity = activityInstance.getActivity();
      
      if (STATE_STARTING.equals(activityInstance.workState)) {
        log.debug("Starting "+activityInstance);
        start(activityInstance);
        
      } else if (STATE_STARTING_MULTI_INSTANCE.equals(activityInstance.workState)) {
        log.debug("Starting multi instance "+activityInstance);
        start(activityInstance);
        
      } else if (STATE_STARTING_MULTI_CONTAINER.equals(activityInstance.workState)) {
        List<Object> values = activityInstance.getValue(activity.multiInstance);
        if (values!=null && !values.isEmpty()) {
          log.debug("Starting multi container "+activityInstance);
          for (Object value: values) {
            ActivityInstanceImpl elementActivityInstance = activityInstance.createActivityInstance(activity);
            elementActivityInstance.setWorkState(STATE_STARTING_MULTI_INSTANCE); 
            elementActivityInstance.initializeForEachElement(activity.multiInstanceElement, value);
          }
        } else {
          log.debug("Skipping empty multi container "+activityInstance);
          activityInstance.onwards();
        }

      } else if (STATE_NOTIFYING.equals(activityInstance.workState)) {
        log.debug("Notifying parent of "+activityInstance);
        activityInstance.parent.ended(activityInstance);
        activityInstance.workState = null;
      }
    }
    if (hasAsyncWork()) {
      log.debug("Going asynchronous "+workflowInstance);
      workflowInstanceStore.flush(workflowInstance);
      ExecutorService executor = workflowEngine.getExecutorService();
      executor.execute(new Runnable(){
        public void run() {
          try {
            work = workAsync;
            workAsync = null;
            workflowInstance.isAsync = true;
            if (updates!=null) {
              getUpdates().isWorkChanged = true;
              getUpdates().isAsyncWorkChanged = true;
            }
            executeWork();
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }});
    } else {
      workflowInstanceStore.flushAndUnlock(workflowInstance);
    }
  }

  protected void start(ActivityInstanceImpl activityInstance) {
    ActivityImpl activity = activityInstance.getActivity();
    activity.activityType.start(activityInstance);
    if (ActivityInstanceImpl.START_WORKSTATES.contains(activityInstance.workState)) {
      activityInstance.setWorkState(ActivityInstanceImpl.STATE_WAITING);
    }
  }
  
  boolean hasAsyncWork() {
    return workAsync!=null && !workAsync.isEmpty();
  }

  boolean hasWork() {
    return work!=null && !work.isEmpty();
  }

  void flushUpdates() {
    workflowEngine.getWorkflowInstanceStore().flush(this);
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
      log.debug("Ends "+this);
      
      if (callerWorkflowInstanceId!=null) {
        WorkflowInstanceQueryImpl processInstanceQuery = workflowEngine.newWorkflowInstanceQuery()
         .workflowInstanceId(callerWorkflowInstanceId)
         .activityInstanceId(callerActivityInstanceId);
        WorkflowInstanceImpl callerProcessInstance = workflowEngine.lockProcessInstanceWithRetry(processInstanceQuery);
        ActivityInstanceImpl callerActivityInstance = callerProcessInstance.findActivityInstance(callerActivityInstanceId);
        if (callerActivityInstance.isEnded()) {
          throw new RuntimeException("Call activity instance "+callerActivityInstance+" is already ended");
        }
        log.debug("Notifying caller "+callerActivityInstance);
        ActivityImpl activityDefinition = callerActivityInstance.getActivity();
        CallActivity callActivity = (CallActivity) activityDefinition.activityType;
        callActivity.calledProcessInstanceEnded(callerActivityInstance, this);
        callerActivityInstance.onwards();
        callerProcessInstance.executeWork();
      }
    }
  }

  public String toString() {
    return "("+(id!=null ? id.toString() : Integer.toString(System.identityHashCode(this)))+"|wi)";
  }

  public void removeLock() {
    setLock(null);
    if (updates!=null) {
      getUpdates().isLockChanged = true;
    }
  }

  public LockImpl getLock() {
    return lock;
  }

  public void setLock(LockImpl lock) {
    this.lock = lock;
    if (updates!=null) {
      getUpdates().isLockChanged = true;
    }
  }
  
  public void setWorkflowId(String processDefinitionId) {
    this.workflowId = processDefinitionId;
  }

  public void setEnd(LocalDateTime end) {
    this.end = end;
    if (start!=null && end!=null) {
      this.duration = new Duration(start.toDateTime(), end.toDateTime()).getMillis();
    }
    if (updates!=null) {
      getUpdates().isEndChanged = true;
    }
  }
  
  public Object getTransientContextObject(String key) {
    return transientContext!=null ? transientContext.get(key) : null;
  }
  
  /** getter for casting convenience */ 
  @Override
  public WorkflowInstanceUpdates getUpdates() {
    return (WorkflowInstanceUpdates) updates;
  }


  @Override
  public String getWorkflowId() {
    return workflowId;
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

  public void trackUpdates(boolean isNew) {
    if (updates==null) {
      updates = new WorkflowInstanceUpdates(isNew);
    } else {
      updates.reset(isNew);
    }
    super.trackUpdates(isNew);
  }
}

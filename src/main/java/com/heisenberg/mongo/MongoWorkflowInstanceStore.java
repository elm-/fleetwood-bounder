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
package com.heisenberg.mongo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.heisenberg.impl.Time;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.WorkflowInstanceQueryImpl;
import com.heisenberg.impl.WorkflowInstanceStore;
import com.heisenberg.impl.WorkflowQueryImpl.Representation;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.impl.instance.WorkflowInstanceUpdates;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


/**
 * @author Walter White
 */
public class MongoWorkflowInstanceStore extends MongoCollection implements WorkflowInstanceStore {
  
  public static final Logger log = MongoWorkflowEngine.log;

  protected WorkflowEngineImpl processEngine;
  protected MongoWorkflowEngineConfiguration.WorkflowInstanceFields fields;
  protected WriteConcern writeConcernStoreProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;
  
  public MongoWorkflowInstanceStore() {
  }

  public MongoWorkflowInstanceStore(ServiceRegistry serviceRegistry) {
  }

  @Override
  public String createWorkflowInstanceId(WorkflowImpl processDefinition) {
    return new ObjectId().toString();
  }

  @Override
  public String createActivityInstanceId() {
    return new ObjectId().toString();
  }

  @Override
  public String createVariableInstanceId() {
    return new ObjectId().toString();
  }

  @Override
  public void insertWorkflowInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbProcessInstance = writeProcessInstance(workflowInstance);
    insert(dbProcessInstance, writeConcernStoreProcessInstance);
    workflowInstance.trackUpdates();
  }
  
  @Override
  public WorkflowInstanceImpl findWorkflowInstanceById(String processInstanceId) {
    BasicDBObject dbProcess = findOne(new BasicDBObject(fields._id, new ObjectId(processInstanceId)));
    return readProcessInstance(dbProcess);
  }

  @Override
  public void flush(WorkflowInstanceImpl workflowInstance) {
    log.debug("Flushing...");
    
    WorkflowInstanceUpdates updates = workflowInstance.getUpdates();
    
    DBObject query = BasicDBObjectBuilder.start()
            .add(fields._id,  new ObjectId(workflowInstance.id))
            .add(fields.lock,  writeLock(workflowInstance.lock))
            .get();
    
    BasicDBObject fieldUpdates = new BasicDBObject();
    BasicDBObject update = new BasicDBObject();

    if (updates.isEndChanged) {
      log.debug("  Workflow instance ended");
      fieldUpdates.append(fields.end, workflowInstance.end);
      fieldUpdates.append(fields.duration, workflowInstance.duration);
    }
    // MongoDB can't combine updates of array elements together with 
    // adding elements to that array.  That's why we overwrite the whole
    // activity instance array when an update happened in there.
    // We do archive the ended (and joined) activity instances into a separate collection 
    // that doesn't have to be loaded.
    if (updates.isActivityInstancesChanged) {
      log.debug("  Activity instances changed");
      List<BasicDBObject> activityInstances = new ArrayList<>();
      List<BasicDBObject> archivedActivityInstances = new ArrayList<>();
      collectActivities(workflowInstance, activityInstances, archivedActivityInstances);
      fieldUpdates.append(fields.activityInstances, activityInstances);
      if (!archivedActivityInstances.isEmpty()) {
        update.append("$push", new BasicDBObject(fields.archivedActivityInstances, archivedActivityInstances));
      }
    } else {
      log.debug("  No activity instances changed");
    }
    
    if (updates.isVariableInstancesChanged) {
      log.debug("  Variable instances changed");
      writeVariables(fieldUpdates, workflowInstance);
    } else {
      log.debug("  No variable instances changed");
    }

    if (updates.isWorkChanged) {
      log.debug("  Work changed");
      writeWork(fieldUpdates, fields.work, workflowInstance.work);
    } else {
      log.debug("  No work changed");
    }

    if (updates.isAsyncWorkChanged) {
      log.debug("  Aync work changed");
      writeWork(fieldUpdates, fields.asyncWork, workflowInstance.asyncWork);
    } else {
      log.debug("  No async work changed");
    }

    if (!fieldUpdates.isEmpty()) {
      update.append("$set", fieldUpdates);
    } else {
      log.debug("  No workflow instance field updates");
    }
    
    if (!update.isEmpty()) {
      update(query, update, false, false, writeConcernFlushUpdates);
    } else {
      log.debug("  Nothing to flush");
    }
    
    // reset the update tracking as all changes have been saved
    workflowInstance.trackUpdates();
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl processInstance) {
    processInstance.lock = null;
    BasicDBObject dbProcessInstance = writeProcessInstance(processInstance);
    saveProcessInstance(dbProcessInstance);
    processInstance.trackUpdates();
  }

  @Override
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQueryImpl workflowInstanceQueryImpl) {
    return null;
  }
  
  public void saveProcessInstance(BasicDBObject dbProcessInstance) {
    save(dbProcessInstance, writeConcernStoreProcessInstance);
  }
  
  public WorkflowInstanceImpl lockWorkflowInstance(WorkflowInstanceQueryImpl processInstanceQuery) {
    BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
    if (processInstanceQuery.processInstanceId!=null) {
      builder.add(fields._id, new ObjectId(processInstanceQuery.processInstanceId));
    }
    if (processInstanceQuery.activityInstanceId!=null) {
      builder.add(fields.activityInstances+"."+fields._id, new ObjectId(processInstanceQuery.activityInstanceId));
    }
    DBObject query = builder 
            .push(fields.lock)
              .add("$exists", false)
            .pop()
            .get(); 
    DBObject update = BasicDBObjectBuilder.start()
            .push("$set")
            .push(fields.lock)
              .add(fields.time, Time.now().toDate())
              .add(fields.owner, processEngine.getId())
            .pop()
          .pop()
          .get();
    BasicDBObject dbProcessInstance = findAndModify(query, update);
    if (dbProcessInstance==null) {
      return null;
    }
    return readProcessInstance(dbProcessInstance);
  }

  public BasicDBObject writeProcessInstance(WorkflowInstanceImpl workflowInstance) {
    BasicDBObject dbProcess = new BasicDBObject();
    writeId(dbProcess, fields._id, workflowInstance.id);
    writeStringOpt(dbProcess, fields.organizationId, workflowInstance.organizationId);
    writeId(dbProcess, fields.workflowId, workflowInstance.processDefinition.id);
    writeTimeOpt(dbProcess, fields.start, workflowInstance.start);
    writeTimeOpt(dbProcess, fields.end, workflowInstance.end);
    writeLongOpt(dbProcess, fields.duration, workflowInstance.duration);
    writeObjectOpt(dbProcess, fields.lock, writeLock(workflowInstance.lock));
    List<BasicDBObject> activityInstances = new ArrayList<>();
    List<BasicDBObject> archivedActivityInstances = new ArrayList<>();
    collectActivities(workflowInstance, activityInstances, archivedActivityInstances);
    writeObjectOpt(dbProcess, fields.activityInstances, activityInstances);
    if (!archivedActivityInstances.isEmpty()) {
      writeObjectOpt(dbProcess, fields.archivedActivityInstances, archivedActivityInstances);
    }
    writeWork(dbProcess, fields.work, workflowInstance.work);
    writeWork(dbProcess, fields.asyncWork, workflowInstance.asyncWork);
    return dbProcess;
  }
  
  protected void writeWork(BasicDBObject dbProcessInstance, String fieldName, Queue<ActivityInstanceImpl> workQueue) {
    List<ObjectId> workActivityInstanceIds = new ArrayList<ObjectId>();
    if (workQueue!=null && !workQueue.isEmpty()) {
      for (ActivityInstanceImpl workActivityInstance: workQueue) {
        workActivityInstanceIds.add(new ObjectId(workActivityInstance.id));
      }
    }
    dbProcessInstance.append(fieldName, workActivityInstanceIds);
  }

  public WorkflowInstanceImpl readProcessInstance(BasicDBObject dbWorkflowInstance) {
    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl();
    workflowInstance.workflowEngine = processEngine;
    workflowInstance.organizationId = readString(dbWorkflowInstance, fields.organizationId);
    workflowInstance.processDefinitionId = readId(dbWorkflowInstance, fields.workflowId);
    WorkflowImpl processDefinition = processEngine.newProcessDefinitionQuery()
            .representation(Representation.EXECUTABLE)
            .id(workflowInstance.processDefinitionId)
            .get();
    workflowInstance.processDefinition = processDefinition;
    workflowInstance.processDefinitionId = processDefinition.id;
    workflowInstance.workflowInstance = workflowInstance;
    workflowInstance.scopeDefinition = workflowInstance.processDefinition;
    workflowInstance.id = readId(dbWorkflowInstance, fields._id);
    workflowInstance.start = readTime(dbWorkflowInstance, fields.start);
    workflowInstance.end = readTime(dbWorkflowInstance, fields.end);
    workflowInstance.duration = readLong(dbWorkflowInstance, fields.duration);
    workflowInstance.lock = readLock((BasicDBObject) dbWorkflowInstance.get(fields.lock));
    
    Map<Object, ActivityInstanceImpl> allActivityInstances = new LinkedHashMap<>();
    Map<Object, Object> parentIds = new HashMap<>();
    List<BasicDBObject> dbActivityInstances = readList(dbWorkflowInstance, fields.activityInstances);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(workflowInstance, dbActivityInstance);
        allActivityInstances.put(activityInstance.id, activityInstance);
        parentIds.put(activityInstance.id, dbActivityInstance.get(fields.parent));
      }
    }
    
    for (ActivityInstanceImpl activityInstance: allActivityInstances.values()) {
      Object parentId = parentIds.get(activityInstance.id);
      activityInstance.parent = (parentId!=null ? allActivityInstances.get(parentId.toString()) : workflowInstance);
      activityInstance.parent.addActivityInstance(activityInstance);
    }
    
    workflowInstance.variableInstances = readVariableInstances(dbWorkflowInstance, workflowInstance);
    workflowInstance.work = readWork(dbWorkflowInstance, fields.work, workflowInstance);
    workflowInstance.asyncWork = readWork(dbWorkflowInstance, fields.asyncWork, workflowInstance);
    
    return workflowInstance;
  }

  @SuppressWarnings("unchecked")
  protected Queue<ActivityInstanceImpl> readWork(BasicDBObject dbWorkflowInstance, String fieldName, WorkflowInstanceImpl workflowInstance) {
    Queue<ActivityInstanceImpl> workQueue = null;
    List<ObjectId> workActivityInstanceIds = (List<ObjectId>) dbWorkflowInstance.get(fieldName);
    if (workActivityInstanceIds!=null) {
      workQueue = new LinkedList<>();
      for (ObjectId workActivityInstanceId: workActivityInstanceIds) {
        ActivityInstanceImpl workActivityInstance = workflowInstance.findActivityInstance(workActivityInstanceId.toString());
        workQueue.add(workActivityInstance);
      }
    }
    return workQueue;
  }

  private List<VariableInstanceImpl> readVariableInstances(BasicDBObject dbWorkflowInstance, ScopeInstanceImpl parent) {
    List<BasicDBObject> dbVariableInstances = readList(dbWorkflowInstance, fields.variableInstances);
    if (dbVariableInstances!=null) {
      for (BasicDBObject dbVariableInstance: dbVariableInstances) {
        VariableInstanceImpl variableInstance = new VariableInstanceImpl();
        variableInstance.processEngine = processEngine;
        variableInstance.processInstance = parent.workflowInstance;
        variableInstance.id = readId(dbVariableInstance, fields._id);
        variableInstance.variableDefinitionId = readString(dbVariableInstance, fields.variableId);
        variableInstance.variableDefinition = parent.workflowInstance.processDefinition.findVariable(variableInstance.variableDefinitionId);
        variableInstance.variableDefinitionId = variableInstance.variableDefinition.id;
        variableInstance.dataType = variableInstance.variableDefinition.dataType;
        variableInstance.value = variableInstance.dataType.convertJsonToInternalValue(dbVariableInstance.get(fields.value));
        parent.addVariableInstance(variableInstance);
      }
    }

    return null;
  }

  protected BasicDBObject writeLock(LockImpl lock) {
    if (lock==null) {
      return null;
    }
    BasicDBObject dbLock = new BasicDBObject();
    writeTimeOpt(dbLock, fields.time, lock.time);
    writeObjectOpt(dbLock, fields.owner, lock.owner);
    return dbLock;
  }
  
  protected LockImpl readLock(BasicDBObject dbLock) {
    if (dbLock==null) {
      return null;
    }
    LockImpl lock = new LockImpl();
    lock.owner = readString(dbLock, fields.owner);
    lock.time = readTime(dbLock, fields.time);
    return lock;
  }
  
  protected void collectActivities(ScopeInstanceImpl scopeInstance, List<BasicDBObject> dbActivityInstances, List<BasicDBObject> dbArchivedActivityInstances) {
    if (scopeInstance.activityInstances!=null) {
      List<ActivityInstanceImpl> activeActivityInstances = new ArrayList<>(); 
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        BasicDBObject dbActivity = writeActivityInstance(activity);
        if (activity.workState!=null) { // null means ready to be archived
          dbActivityInstances.add(dbActivity);
          activeActivityInstances.add(activity);
        } else {
          dbArchivedActivityInstances.add(dbActivity);
        }
        collectActivities(activity, dbActivityInstances, dbArchivedActivityInstances);
      }
      scopeInstance.activityInstances = activeActivityInstances;
    }
  }

  protected BasicDBObject writeActivityInstance(ActivityInstanceImpl activityInstance) {
    String parentId = (activityInstance.parent.isProcessInstance() ? null : activityInstance.parent.getId());
    BasicDBObject dbActivity = new BasicDBObject();
    writeId(dbActivity, fields._id, activityInstance.id);
    writeStringOpt(dbActivity, fields.activityId, activityInstance.activityDefinitionId);
    writeStringOpt(dbActivity, fields.workState, activityInstance.workState);
    writeIdOpt(dbActivity, fields.parent, parentId);
    writeTimeOpt(dbActivity, fields.start, activityInstance.start);
    writeTimeOpt(dbActivity, fields.end, activityInstance.end);
    writeLongOpt(dbActivity, fields.duration, activityInstance.duration);
    return dbActivity;
  }
  
  protected ActivityInstanceImpl readActivityInstance(WorkflowInstanceImpl processInstance, BasicDBObject dbActivityInstance) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = readId(dbActivityInstance, fields._id);
    activityInstance.start = readTime(dbActivityInstance, fields.start);
    activityInstance.end = readTime(dbActivityInstance, fields.end);
    activityInstance.duration = readLong(dbActivityInstance, fields.duration);
    activityInstance.workState = readString(dbActivityInstance, fields.workState);
    activityInstance.activityDefinitionId = readString(dbActivityInstance, fields.activityId);
    activityInstance.workflowEngine = processEngine;
    activityInstance.processDefinition = processInstance.processDefinition;
    activityInstance.activityDefinition = processInstance.processDefinition.findActivity(activityInstance.activityDefinitionId);
    activityInstance.activityDefinitionId = activityInstance.activityDefinition.id;
    activityInstance.scopeDefinition = activityInstance.activityDefinition;
    activityInstance.workflowInstance = processInstance;
    activityInstance.variableInstances = readVariableInstances(dbActivityInstance, activityInstance);
    return activityInstance;
  }

  protected void writeVariables(BasicDBObject dbScope, ScopeInstanceImpl scope) {
    if (scope.variableInstances!=null) {
      ScopeInstanceImpl parent = scope.getParent();
      String parentId = (parent!=null ? parent.getId() : null);
      for (VariableInstanceImpl variable: scope.variableInstances) {
        BasicDBObject dbVariable = new BasicDBObject();
        writeId(dbVariable, fields._id, variable.id);
        writeString(dbVariable, fields.variableId, variable.variableDefinitionId);
        writeIdOpt(dbVariable, fields.parent, parentId);
        Object jsonValue = variable.dataType.convertInternalToJsonValue(variable.value);
        writeObjectOpt(dbVariable, fields.value, jsonValue);
        writeListElementOpt(dbScope, fields.variableInstances, dbVariable);
      }
    }
  }
  
  public WorkflowEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public MongoWorkflowEngineConfiguration.WorkflowInstanceFields getFields() {
    return fields;
  }

  public WriteConcern getWriteConcernStoreProcessInstance() {
    return writeConcernStoreProcessInstance;
  }
  
  public WriteConcern getWriteConcernFlushUpdates() {
    return writeConcernFlushUpdates;
  }
}

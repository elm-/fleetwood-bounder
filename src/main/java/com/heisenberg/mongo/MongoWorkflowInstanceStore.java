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
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.heisenberg.impl.WorkflowQueryImpl.Representation;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.WorkflowInstanceQueryImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.WorkflowInstanceStore;
import com.heisenberg.impl.definition.WorkflowImpl;
import com.heisenberg.impl.engine.operation.NotifyEndOperation;
import com.heisenberg.impl.engine.operation.Operation;
import com.heisenberg.impl.engine.operation.StartActivityInstanceOperation;
import com.heisenberg.impl.engine.updates.OperationAddNotifyEndUpdate;
import com.heisenberg.impl.engine.updates.OperationAddStartUpdate;
import com.heisenberg.impl.engine.updates.Update;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.WorkflowInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.plugin.ServiceRegistry;
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
  protected MongoUpdateConverters updateConverters;
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
  public void insertWorkflowInstance(WorkflowInstanceImpl processInstance) {
    BasicDBObject dbProcessInstance = writeProcessInstance(processInstance);
    insert(dbProcessInstance, writeConcernStoreProcessInstance);
  }
  
  @Override
  public WorkflowInstanceImpl findWorkflowInstanceById(String processInstanceId) {
    BasicDBObject dbProcess = findOne(new BasicDBObject(fields._id, new ObjectId(processInstanceId)));
    return readProcessInstance(dbProcess);
  }

  @Override
  public void flush(WorkflowInstanceImpl processInstance) {
    List<Update> updates = processInstance.getUpdates();
    if (updates!=null) {
      List<BasicDBObject> dbUpdates = new ArrayList<>(); 
      for (Update update : updates) {
        BasicDBObject dbUpdate = updateConverters.toDbUpdate(update);
        if (dbUpdate!=null) {
          dbUpdates.add(dbUpdate);
        }
      }
      flushUpdates(processInstance.id, processInstance.lock, dbUpdates);
      // After the first and all subsequent flushes, we need to capture the updates so we initialize the collection
      // @see ProcessInstanceImpl.updates
      processInstance.setUpdates(new ArrayList<Update>());
    } else {
      // As long as the process instance is not saved, the updates collection is null.
      // That means it's not yet necessary to collect the updates. 
      // @see ProcessInstanceImpl.updates
      log.debug("Just saved, no flush needed");
      // The operations in the process instance will not be serialized.
      // When the process instance starts, the first activity instance operations 
      // will be not added to the updates because updates==null.  Therefore, we 
      // convert them here to updates.  After this method, all further operations 
      // will be recorded normally because updates!=null.
      updates = new ArrayList<Update>();
      if (processInstance.operations!=null) {
        for (Operation operation: processInstance.operations) {
          if (operation instanceof StartActivityInstanceOperation) {
            updates.add(new OperationAddStartUpdate(operation.activityInstance));
          } else if (operation instanceof NotifyEndOperation) {
            updates.add(new OperationAddNotifyEndUpdate(operation.activityInstance));
          } else {
            throw new RuntimeException("Unsupported operation type: "+operation.getClass().getName());
          }
        }
      }
      processInstance.setUpdates(updates);
    }
  }

  @Override
  public void flushAndUnlock(WorkflowInstanceImpl processInstance) {
    processInstance.lock = null;
    BasicDBObject dbProcessInstance = writeProcessInstance(processInstance);
    saveProcessInstance(dbProcessInstance);
    processInstance.setUpdates(new ArrayList<Update>());
  }

  @Override
  public List<WorkflowInstanceImpl> findWorkflowInstances(WorkflowInstanceQueryImpl workflowInstanceQueryImpl) {
    return null;
  }
  
  public void flushUpdates(String processInstanceId, LockImpl lock, List<BasicDBObject> dbUpdates) {
    DBObject query = BasicDBObjectBuilder.start()
            .add(fields._id,  new ObjectId(processInstanceId))
            .add(fields.lock,  writeLock(lock))
            .get();
    DBObject update = new BasicDBObject("$pushAll", new BasicDBObject(fields.updates, dbUpdates));
    update(query, update, false, false, writeConcernFlushUpdates);
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

  public BasicDBObject writeProcessInstance(WorkflowInstanceImpl process) {
    BasicDBObject dbProcess = new BasicDBObject();
    writeId(dbProcess, fields._id, process.id);
    writeStringOpt(dbProcess, fields.organizationId, process.organizationId);
    writeId(dbProcess, fields.workflowId, process.processDefinition.id);
    writeTimeOpt(dbProcess, fields.start, process.start);
    writeTimeOpt(dbProcess, fields.end, process.end);
    writeLongOpt(dbProcess, fields.duration, process.duration);
    writeObjectOpt(dbProcess, fields.lock, writeLock(process.lock));
    writeActivities(dbProcess, process);
    writeVariables(dbProcess, process);
    return dbProcess;
  }
  
  public WorkflowInstanceImpl readProcessInstance(BasicDBObject dbProcess) {
    WorkflowInstanceImpl process = new WorkflowInstanceImpl();
    process.processEngine = processEngine;
    process.organizationId = readString(dbProcess, fields.organizationId);
    process.processDefinitionId = readId(dbProcess, fields.workflowId);
    WorkflowImpl processDefinition = processEngine.newProcessDefinitionQuery()
            .representation(Representation.EXECUTABLE)
            .id(process.processDefinitionId)
            .get();
    process.processDefinition = processDefinition;
    process.processInstance = process;
    process.scopeDefinition = process.processDefinition;
    process.id = readId(dbProcess, fields._id);
    process.start = readTime(dbProcess, fields.start);
    process.end = readTime(dbProcess, fields.end);
    process.duration = readLong(dbProcess, fields.duration);
    process.lock = readLock((BasicDBObject) dbProcess.get(fields.lock));
    
    Map<Object, ActivityInstanceImpl> allActivityInstances = new LinkedHashMap<>();
    Map<Object, Object> parentIds = new HashMap<>();
    List<BasicDBObject> dbActivityInstances = readList(dbProcess, fields.activityInstances);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(process, dbActivityInstance);
        allActivityInstances.put(activityInstance.id, activityInstance);
        parentIds.put(activityInstance.id, dbActivityInstance.get(fields.parent));
      }
    }
    
    for (ActivityInstanceImpl activityInstance: allActivityInstances.values()) {
      Object parentId = parentIds.get(activityInstance.id);
      activityInstance.parent = (parentId!=null ? allActivityInstances.get(parentId.toString()) : process);
      activityInstance.parent.addActivityInstance(activityInstance);
    }
    
    parentIds = new HashMap<>();
    Map<Object, VariableInstanceImpl> allVariableInstances = new HashMap<>();
    List<BasicDBObject> dbVariableInstances = readList(dbProcess, fields.variableInstances);
    if (dbVariableInstances!=null) {
      for (BasicDBObject dbVariableInstance: dbVariableInstances) {
        VariableInstanceImpl variableInstance = readVariableInstance(process, dbVariableInstance);
        allVariableInstances.put(variableInstance.id, variableInstance);
        parentIds.put(variableInstance.id, dbVariableInstance.get(fields.parent));
      }
    }

    for (VariableInstanceImpl variableInstance: allVariableInstances.values()) {
      Object parentId = parentIds.get(variableInstance.id);
      variableInstance.parent = (parentId!=null ? allActivityInstances.get(parentId) : process);
      variableInstance.parent.addVariableInstance(variableInstance);
    }

    return process;
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

  protected void writeActivities(BasicDBObject dbProcess, ScopeInstanceImpl scopeInstance) {
    if (scopeInstance.activityInstances!=null) {
      String parentId = (scopeInstance.isProcessInstance() ? null : scopeInstance.getId());
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        BasicDBObject dbActivity = new BasicDBObject();
        writeId(dbActivity, fields._id, activity.id);
        writeStringOpt(dbActivity, fields.activityId, activity.activityDefinitionId);
        writeIdOpt(dbActivity, fields.parent, parentId);
        writeTimeOpt(dbActivity, fields.start, activity.start);
        writeTimeOpt(dbActivity, fields.end, activity.end);
        writeLongOpt(dbActivity, fields.duration, activity.duration);
        writeListElementOpt(dbProcess, fields.activityInstances, dbActivity);
        writeActivities(dbProcess, activity);
      }
    }
  }
  
  protected ActivityInstanceImpl readActivityInstance(WorkflowInstanceImpl processInstance, BasicDBObject dbActivityInstance) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = readId(dbActivityInstance, fields._id);
    activityInstance.start = readTime(dbActivityInstance, fields.start);
    activityInstance.end = readTime(dbActivityInstance, fields.end);
    activityInstance.duration = readLong(dbActivityInstance, fields.duration);
    activityInstance.activityDefinitionId = readString(dbActivityInstance, fields.activityId);
    activityInstance.processEngine = processEngine;
    activityInstance.processDefinition = processInstance.processDefinition;
    activityInstance.activityDefinition = processInstance.processDefinition.findActivity(activityInstance.activityDefinitionId);
    activityInstance.scopeDefinition = activityInstance.activityDefinition;
    activityInstance.processInstance = processInstance; 
    return activityInstance;
  }

  protected void writeVariables(BasicDBObject dbProcess, ScopeInstanceImpl scopeInstance) {
    if (scopeInstance.variableInstances!=null) {
      ScopeInstanceImpl parent = scopeInstance.getParent();
      String parentId = (parent!=null ? parent.getId() : null);
      for (VariableInstanceImpl variable: scopeInstance.variableInstances) {
        BasicDBObject dbVariable = new BasicDBObject();
        writeId(dbVariable, fields._id, variable.id);
        writeString(dbVariable, fields.variableId, variable.variableDefinitionId);
        writeIdOpt(dbVariable, fields.parent, parentId);
        Object jsonValue = variable.dataType.convertInternalToJsonValue(variable.value);
        writeObjectOpt(dbVariable, fields.value, jsonValue);
        writeListElementOpt(dbProcess, fields.variableInstances, dbVariable);
      }
    }
    if (scopeInstance.activityInstances!=null) {
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        writeVariables(dbProcess, activity);
      }
    }
  }

  protected VariableInstanceImpl readVariableInstance(WorkflowInstanceImpl processInstance, BasicDBObject dbVariableInstance) {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.processEngine = processEngine;
    variableInstance.processInstance = processInstance;
    variableInstance.id = readId(dbVariableInstance, fields._id);
    variableInstance.variableDefinitionId = readString(dbVariableInstance, fields.variableId);
    variableInstance.variableDefinition = processInstance.processDefinition.findVariable(variableInstance.variableDefinitionId);
    variableInstance.dataType = variableInstance.variableDefinition.dataType;
    variableInstance.value = variableInstance.dataType.convertJsonToInternalValue(dbVariableInstance.get(fields.value));
    return variableInstance;
  }

  
  public WorkflowEngineImpl getProcessEngine() {
    return processEngine;
  }

  
  public MongoWorkflowEngineConfiguration.WorkflowInstanceFields getFields() {
    return fields;
  }

  
  public MongoUpdateConverters getUpdateConverters() {
    return updateConverters;
  }

  
  public WriteConcern getWriteConcernStoreProcessInstance() {
    return writeConcernStoreProcessInstance;
  }

  
  public WriteConcern getWriteConcernFlushUpdates() {
    return writeConcernFlushUpdates;
  }
}

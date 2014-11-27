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
package com.heisenberg.impl.engine.mongodb;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


/**
 * @author Walter White
 */
public class MongoProcessInstances extends MongoCollection {
  
  public static final Logger log = LoggerFactory.getLogger(MongoProcessInstances.class);
  
  public static class Fields {
    public String _id = "_id";
    public String processDefinitionId = "pd";
    public String start = "s";
    public String end = "e";
    public String duration = "d";
    public String activityInstances = "a";
    public String variableInstances = "v";
    public String parent = "p";
    public String variableDefinitionId = "vd";
    public String value = "vl";
    public String activityDefinitionId = "ad";
    public String lock = "l";
    public String time = "t";
    public String owner= "w";
    public String updates = "u";
    public String operations = "o";
  }
  
  protected MongoProcessEngine processEngine;
  protected Fields fields;
  protected DBCollection dbCollection;
  
  protected WriteConcern writeConcernStoreProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;

  public MongoProcessInstances(MongoProcessEngine processEngine, DB db, MongoConfiguration mongoConfiguration) {
    super(db, mongoConfiguration.processInstancesCollectionName);
    this.processEngine = processEngine;
    this.fields = mongoConfiguration.processInstanceFields!=null ? mongoConfiguration.processInstanceFields : new Fields();
    this.dbCollection = db.getCollection("processInstances");
    this.writeConcernStoreProcessInstance = getWriteConcern(mongoConfiguration.writeConcernInsertProcessInstance);
    this.writeConcernFlushUpdates = getWriteConcern(mongoConfiguration.writeConcernFlushUpdates);
    this.isPretty = mongoConfiguration.isPretty;
  }
  
  public void insertProcessInstance(ProcessInstanceImpl processInstance) {
    BasicDBObject dbProcessInstance = writeProcessInstance(processInstance);
    insert(dbProcessInstance, writeConcernStoreProcessInstance);
  }
  
  public void flushUpdates(Object processInstanceId, List<BasicDBObject> dbUpdates) {
    this.dbCollection.update(
        new BasicDBObject(fields._id,  processInstanceId),
        new BasicDBObject("$pushAll", new BasicDBObject(fields.updates, dbUpdates)),
        false, false, writeConcernFlushUpdates);
  }

  public void saveProcessInstance(BasicDBObject dbProcessInstance) {
    save(dbProcessInstance, writeConcernStoreProcessInstance);
  }

  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(Object activityInstanceId) {
    DBObject query = BasicDBObjectBuilder.start()
            .add(fields.activityInstances+"."+fields._id, activityInstanceId)
            .push(fields.lock)
              .add("$exists", false)
            .pop()
            .get(); 
    DBObject update = BasicDBObjectBuilder.start()
            .push("$set")
            .push(fields.lock)
              .add(fields.time, Time.now().toDate())
              .add(fields.owner, processEngine.id)
            .pop()
          .pop()
          .get();
    BasicDBObject dbProcessInstance = findAndModify(query, update);
    return readProcessInstance(dbProcessInstance);
  }

  public DBObject queryUnlockedAndActivityInstanceId(Object activityInstanceId) {
    return BasicDBObjectBuilder.start()
      .add(fields.activityInstances+"."+fields._id, activityInstanceId)
      .push(fields.lock)
        .add("$exists", false)
      .pop()
      .get();
  }
  
  public DBObject updateLock(String processEngineId) {
    return BasicDBObjectBuilder.start()
      .push("$set")
        .push(fields.lock)
          .add(fields.time, Time.now().toDate())
          .add(fields.owner, processEngineId)
        .pop()
      .pop()
      .get();
  }

  public BasicDBObject writeProcessInstance(ProcessInstanceImpl process) {
    BasicDBObject dbProcess = new BasicDBObject();
    writeObject(dbProcess, fields._id, process.id);
    writeObject(dbProcess, fields.processDefinitionId, process.processDefinition.id);
    writeTimeOpt(dbProcess, fields.start, process.start);
    writeTimeOpt(dbProcess, fields.end, process.end);
    writeLongOpt(dbProcess, fields.duration, process.duration);
    writeObjectOpt(dbProcess, fields.lock, writeLock(process.lock));
    writeActivities(dbProcess, process);
    writeVariables(dbProcess, process);
    
    return dbProcess;
  }
  
  public ProcessInstanceImpl readProcessInstance(BasicDBObject dbProcess) {
    ProcessInstanceImpl process = new ProcessInstanceImpl();
    process.processEngine = processEngine;
    process.processDefinitionId = dbProcess.get(fields.processDefinitionId);
    ProcessDefinitionImpl processDefinition = processEngine.findProcessDefinitionByIdUsingCache(process.processDefinitionId);
    process.processDefinition = processDefinition;
    process.processInstance = process;
    process.scopeDefinition = process.processDefinition;
    process.id = dbProcess.get(fields._id);
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
      activityInstance.parent = (parentId!=null ? allActivityInstances.get(parentId) : process);
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
      Object parentId = (scopeInstance.isProcessInstance() ? null : scopeInstance.getId());
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        BasicDBObject dbActivity = new BasicDBObject();
        writeObject(dbActivity, fields._id, activity.id);
        writeObject(dbActivity, fields.activityDefinitionId, activity.activityDefinitionId);
        writeObjectOpt(dbActivity, fields.parent, parentId);
        writeTimeOpt(dbActivity, fields.start, activity.start);
        writeTimeOpt(dbActivity, fields.end, activity.end);
        writeLongOpt(dbActivity, fields.duration, activity.duration);
        writeListElementOpt(dbProcess, fields.activityInstances, dbActivity);
        writeActivities(dbProcess, activity);
      }
    }
  }
  
  protected ActivityInstanceImpl readActivityInstance(ProcessInstanceImpl processInstance, BasicDBObject dbActivityInstance) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = dbActivityInstance.get(fields._id);
    activityInstance.start = readTime(dbActivityInstance, fields.start);
    activityInstance.end = readTime(dbActivityInstance, fields.end);
    activityInstance.duration = readLong(dbActivityInstance, fields.duration);
    activityInstance.activityDefinitionId = dbActivityInstance.get(fields.activityDefinitionId);
    activityInstance.processEngine = processEngine;
    activityInstance.processDefinition = processInstance.processDefinition;
    activityInstance.activityDefinition = processInstance.processDefinition.findActivityDefinition(activityInstance.activityDefinitionId);
    activityInstance.scopeDefinition = activityInstance.activityDefinition;
    activityInstance.processInstance = processInstance; 
    return activityInstance;
  }

  protected void writeVariables(BasicDBObject dbProcess, ScopeInstanceImpl scopeInstance) {
    if (scopeInstance.variableInstances!=null) {
      ScopeInstanceImpl parent = scopeInstance.getParent();
      Object parentId = (parent!=null ? parent.getId() : null);
      for (VariableInstanceImpl variable: scopeInstance.variableInstances) {
        BasicDBObject dbVariable = new BasicDBObject();
        writeObject(dbVariable, fields._id, variable.id);
        writeObject(dbVariable, fields.variableDefinitionId, variable.variableDefinitionId);
        writeObjectOpt(dbVariable, fields.parent, parentId);
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

  protected VariableInstanceImpl readVariableInstance(ProcessInstanceImpl processInstance, BasicDBObject dbVariableInstance) {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.processEngine = processEngine;
    variableInstance.processInstance = processInstance;
    variableInstance.id = dbVariableInstance.get(fields._id);
    variableInstance.variableDefinitionId = dbVariableInstance.get(fields.variableDefinitionId);
    variableInstance.variableDefinition = processInstance.processDefinition.findVariableDefinition(variableInstance.variableDefinitionId);
    variableInstance.dataType = variableInstance.variableDefinition.dataType;
    variableInstance.dataTypeId = variableInstance.dataType.getTypeId();
    variableInstance.value = variableInstance.dataType.convertJsonToInternalValue(dbVariableInstance.get(fields.value));
    return variableInstance;
  }
}

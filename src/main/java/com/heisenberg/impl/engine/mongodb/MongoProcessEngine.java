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

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.Page;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.impl.ActivityInstanceQueryImpl;
import com.heisenberg.impl.ProcessDefinitionQuery;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQuery;
import com.heisenberg.impl.SimpleProcessDefinitionCache;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessDefinitionFieldNames;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessInstanceFieldNames;
import com.heisenberg.impl.engine.operation.NotifyEndOperation;
import com.heisenberg.impl.engine.operation.Operation;
import com.heisenberg.impl.engine.operation.StartActivityInstanceOperation;
import com.heisenberg.impl.engine.updates.OperationAddNotifyEndUpdate;
import com.heisenberg.impl.engine.updates.OperationAddStartUpdate;
import com.heisenberg.impl.engine.updates.Update;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;



/**
 * @author Walter White
 */
public class MongoProcessEngine extends ProcessEngineImpl {
  
  public static final Logger log = LoggerFactory.getLogger(MongoProcessEngine.class);
  
  protected DB db;
  protected DBCollection processDefinitions;
  protected DBCollection processInstances;
  
  protected WriteConcern writeConcernStoreProcessDefinition;
  protected WriteConcern writeConcernStoreProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;

  protected MongoProcessDefinitionWriter processDefinitionWriter;
  protected MongoProcessDefinitionReader processDefinitionReader;
  protected ProcessDefinitionFieldNames processDefinitionFieldNames;

  protected MongoProcessInstanceMapper processInstanceMapper;
  protected ProcessInstanceFieldNames processInstanceFieldNames;
  
  protected MongoUpdateConverters updateConverters = new MongoUpdateConverters(json);
  
  public MongoProcessEngine(MongoConfiguration mongoDbConfiguration) {
    MongoClient mongoClient = new MongoClient(
            mongoDbConfiguration.serverAddresses, 
            mongoDbConfiguration.credentials, 
            mongoDbConfiguration.optionBuilder.build());
    this.db = mongoClient.getDB(mongoDbConfiguration.databaseName);
    this.processDefinitions = db.getCollection("processDefinitions");
    this.processInstances = db.getCollection("processInstances");
    this.writeConcernStoreProcessDefinition = getWriteConcern(mongoDbConfiguration.writeConcernStoreProcessDefinition, processDefinitions);
    this.writeConcernStoreProcessInstance = getWriteConcern(mongoDbConfiguration.writeConcernStoreProcessInstance, processInstances);
    this.writeConcernFlushUpdates = getWriteConcern(mongoDbConfiguration.writeConcernFlushUpdates, processInstances);
    this.processDefinitionWriter = new MongoProcessDefinitionWriter(this, mongoDbConfiguration.processDefinitionFieldNames);
    this.processDefinitionReader = new MongoProcessDefinitionReader(this, mongoDbConfiguration.processDefinitionFieldNames);
    this.processInstanceMapper = new MongoProcessInstanceMapper(this);
    this.processDefinitionFieldNames = mongoDbConfiguration.processDefinitionFieldNames;
    this.processInstanceFieldNames = mongoDbConfiguration.processInstanceFieldNames;
    this.updateConverters = new MongoUpdateConverters(json);
  }
  
  @Override
  protected void initializeProcessDefinitionCache() {
    processDefinitionCache = new SimpleProcessDefinitionCache();
  }

  @Override
  protected Object createProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return new ObjectId();
  }
  
  @Override
  protected Object createProcessInstanceId(ProcessDefinitionImpl processDefinition) {
    return new ObjectId();
  }

  @Override
  protected Object createActivityInstanceId() {
    return new ObjectId();
  }

  @Override
  protected Object createVariableInstanceId() {
    return new ObjectId();
  }

  @Override
  protected void storeProcessDefinition(ProcessDefinitionImpl processDefinition) {
    BasicDBObject dbProcessDefinition = processDefinitionWriter.writeProcessDefinition(processDefinition);
    log.debug("--processDefinitions-> insert "+PrettyPrinter.toJsonPrettyPrint(dbProcessDefinition));
    WriteResult writeResult = this.processDefinitions.insert(dbProcessDefinition, writeConcernStoreProcessDefinition);
    log.debug("<-processDefinitions-- "+writeResult);
  }

  @Override
  protected ProcessDefinitionImpl loadProcessDefinitionById(Object processDefinitionId) {
    log.debug("--processDefinitions-> find "+processDefinitionId);
    BasicDBObject dbProcess = (BasicDBObject) this.processDefinitions.findOne(new BasicDBObject(processDefinitionFieldNames._id, processDefinitionId));
    log.debug("<-processDefinitions-- "+PrettyPrinter.toJsonPrettyPrint(dbProcess));
    return processDefinitionReader.readProcessDefinition(dbProcess);
  }

  @Override
  public void saveProcessInstance(ProcessInstanceImpl processInstance) {
    BasicDBObject dbProcessInstance = processInstanceMapper.writeProcessInstance(processInstance);
    log.debug("--processInstances-> insert "+PrettyPrinter.toJsonPrettyPrint(dbProcessInstance));
    WriteResult writeResult = this.processInstances.insert(dbProcessInstance, writeConcernStoreProcessInstance);
    log.debug("<-processInstances-- "+writeResult);
  }

  @Override
  public void flush(ProcessInstanceImpl processInstance) {
    List<Update> updates = processInstance.getUpdates();
    if (updates!=null) {
      log.debug("Flushing updates: ");
      List<BasicDBObject> dbUpdates = new ArrayList<>(); 
      for (Update update : updates) {
        BasicDBObject dbUpdate = updateConverters.toDbUpdate(update);
        if (dbUpdate!=null) {
          log.debug("  " + dbUpdate);
          dbUpdates.add(dbUpdate);
        }
      }
      this.processInstances.update(
        new BasicDBObject(processInstanceFieldNames._id,  processInstance.id),
        new BasicDBObject("$pushAll", new BasicDBObject(processInstanceFieldNames.updates, dbUpdates)),
        false, false, writeConcernFlushUpdates);
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
  public void flushAndUnlock(ProcessInstanceImpl processInstance) {
    processInstance.lock = null;
    BasicDBObject dbProcessInstance = processInstanceMapper.writeProcessInstance(processInstance);
    log.debug("--processInstances-> save "+PrettyPrinter.toJsonPrettyPrint(dbProcessInstance));
    WriteResult writeResult = this.processInstances.save(dbProcessInstance, writeConcernStoreProcessInstance);
    log.debug("<-processInstances-- "+writeResult);
    processInstance.setUpdates(new ArrayList<Update>());
  }

  @Override
  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(Object activityInstanceId) {
    DBObject update = processInstanceMapper.updateLock(id);
    DBObject query = processInstanceMapper.queryUnlockedAndActivityInstanceId(activityInstanceId); 
    log.debug("--processInstances-> findAndModify q="+PrettyPrinter.toJsonPrettyPrint(query)+" u="+PrettyPrinter.toJsonPrettyPrint(update));
    BasicDBObject dbProcessInstance = (BasicDBObject) processInstances.findAndModify(query, update);
    log.debug("<-processInstances-- "+dbProcessInstance);
    return processInstanceMapper.readProcessInstance(dbProcessInstance);
  }

  @Override
  public List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQuery processInstanceQuery) {
    return null;
  }

  @Override
  public List<ProcessDefinitionImpl> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery) {
    return null;
  }

  @Override
  public Page<ActivityInstance> findActivityInstances(ActivityInstanceQueryImpl activityInstanceQueryImpl) {
    return null;
  }

  protected WriteConcern getWriteConcern(WriteConcern configuredWriteConcern, DBCollection dbCollection) {
    if (configuredWriteConcern!=null) {
      return configuredWriteConcern;
    }
    return dbCollection.getWriteConcern();
  }
}

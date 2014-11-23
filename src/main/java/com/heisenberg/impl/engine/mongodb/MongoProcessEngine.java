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

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.Page;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.util.ActivityInstanceId;
import com.heisenberg.api.util.ProcessDefinitionId;
import com.heisenberg.impl.ActivityInstanceQueryImpl;
import com.heisenberg.impl.ProcessDefinitionQuery;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQuery;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessDefinitionFieldNames;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
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
  
  protected ProcessDefinitionFieldNames processDefinitionFieldNames;
  protected WriteConcern writeConcernStoreProcessDefinition;
  protected MongoProcessDefinitionDeserializer deserializer;
  
  public MongoProcessEngine(MongoConfiguration mongoDbConfiguration) {
    MongoClient mongoClient = new MongoClient(
            mongoDbConfiguration.serverAddresses, 
            mongoDbConfiguration.credentials, 
            mongoDbConfiguration.optionBuilder.build());
    this.db = mongoClient.getDB(mongoDbConfiguration.databaseName);
    this.processDefinitions = db.getCollection("processDefinitions");
    this.processInstances = db.getCollection("processInstances");
    this.processDefinitionFieldNames = mongoDbConfiguration.processDefinitionFieldNames;
    this.writeConcernStoreProcessDefinition = getWriteConcern(mongoDbConfiguration.writeConcernStoreProcessDefinition, processDefinitions);
    this.deserializer = new MongoProcessDefinitionDeserializer(this, processDefinitionFieldNames);
  }

  @Override
  protected ProcessDefinitionId generateProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return new ProcessDefinitionId(new ObjectId());
  }

  @Override
  protected void storeProcessDefinition(ProcessDefinitionImpl processDefinition) {
    MongoProcessDefinitionSerializer serializer = new MongoProcessDefinitionSerializer(this,processDefinitionFieldNames);
    processDefinition.visit(serializer);
    log.debug("--processDefinitions-> insert "+PrettyPrinter.toJsonPrettyPrint(serializer.dbProcess));
    WriteResult writeResult = this.processDefinitions.insert(serializer.dbProcess, writeConcernStoreProcessDefinition);
    log.debug("<-processDefinitions-- "+writeResult);
  }

  @Override
  protected ProcessDefinitionImpl loadProcessDefinitionById(ProcessDefinitionId processDefinitionId) {
    log.debug("--processDefinitions-> find "+processDefinitionId);
    BasicDBObject dbProcess = (BasicDBObject) this.processDefinitions.findOne(new BasicDBObject(processDefinitionFieldNames._id, processDefinitionId.getInternal()));
    log.debug("<-processDefinitions-- "+PrettyPrinter.toJsonPrettyPrint(dbProcess));
    return deserializer.readProcessDefinition(dbProcess);
  }

  @Override
  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId) {
    return null;
  }

  @Override
  public void saveProcessInstance(ProcessInstanceImpl processInstance) {
  }

  @Override
  public void flush(ProcessInstanceImpl processInstance) {
  }

  @Override
  public void flushAndUnlock(ProcessInstanceImpl processInstance) {
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

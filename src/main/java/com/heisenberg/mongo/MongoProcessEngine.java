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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngineConfiguration;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.memory.MemoryTaskService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;



/**
 * @author Walter White
 */
public class MongoProcessEngine extends ProcessEngineImpl {
  
  public static final Logger dbLog = LoggerFactory.getLogger(MongoProcessEngine.class+".DB");
  public static final Logger log = LoggerFactory.getLogger(MongoProcessEngine.class);
  
  protected DB db;

  public MongoProcessEngine() {
    this(new MongoProcessEngineConfiguration());
  }
  
  public MongoProcessEngine(MongoProcessEngineConfiguration configuration) {
    super(configuration);
  }
  
  @Override
  protected void initializeStorageServices(ProcessEngineConfiguration cfg) {
    MongoProcessEngineConfiguration configuration = (MongoProcessEngineConfiguration) cfg;
    JsonService jsonService = serviceRegistry.getService(JsonService.class);
    
    MongoClient mongoClient = new MongoClient(
            configuration.getServerAddresses(), 
            configuration.getCredentials(), 
            configuration.getOptionBuilder().build());
    
    this.db = mongoClient.getDB(configuration.getDatabaseName());
    configuration.registerService(db);

    boolean isPretty = configuration.isPretty();

    MongoProcessDefinitions processDefinitions = new MongoProcessDefinitions(serviceRegistry);
    DBCollection processDefinitionDbCollection = db.getCollection(configuration.getProcessDefinitionsCollectionName());
    processDefinitions.dbCollection = processDefinitionDbCollection;
    processDefinitions.isPretty = isPretty;
    processDefinitions.fields = configuration.getProcessDefinitionFields();
    processDefinitions.writeConcernInsertProcessDefinition = processDefinitions.getWriteConcern(configuration.getWriteConcernInsertProcessDefinition());
    configuration.registerService(processDefinitions);

    MongoUpdateConverters mongoUpdateConverters = new MongoUpdateConverters(serviceRegistry);
    mongoUpdateConverters.jsonService = jsonService;
    configuration.registerService(mongoUpdateConverters);

    MongoProcessInstances processInstances = new MongoProcessInstances(serviceRegistry);
    DBCollection processInstancesDbCollection = db.getCollection(configuration.getProcessInstancesCollectionName());
    processInstances.processEngine = this;
    processInstances.dbCollection = processInstancesDbCollection;
    processInstances.isPretty = isPretty;
    processInstances.fields = configuration.getProcessInstanceFields();
    processInstances.dbCollection = db.getCollection("processInstances");
    processInstances.writeConcernStoreProcessInstance = processInstances.getWriteConcern(configuration.getWriteConcernInsertProcessInstance());
    processInstances.writeConcernFlushUpdates = processInstances.getWriteConcern(configuration.getWriteConcernFlushUpdates());
    processInstances.updateConverters = mongoUpdateConverters;
    configuration.registerService(processInstances);

    // TODO
    // MongoTaskService mongoTaskService = new MongoTaskService();
    configuration.registerService(new MemoryTaskService());
    
    MongoJobs mongoJobs = new MongoJobs(serviceRegistry);
    DBCollection jobsDbCollection = db.getCollection(configuration.getJobsCollectionName());
    mongoJobs.dbCollection = jobsDbCollection;
    mongoJobs.isPretty = configuration.isPretty();
    mongoJobs.fields = configuration.getJobFields();
    mongoJobs.writeConcernJobs = mongoJobs.getWriteConcern(configuration.getWriteConcernJobs());
    mongoJobs.lockOwner = configuration.getId();
    mongoJobs.jsonService = jsonService;
    configuration.registerService(mongoJobs);

    MongoJobService mongoJobService = new MongoJobService(serviceRegistry);
    mongoJobService.jobs = mongoJobs;
    configuration.registerService(mongoJobService);
  }
  
  public DB getDb() {
    return db;
  }
  
  public void setDb(DB db) {
    this.db = db;
  }
}

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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;


/**
 * @author Walter White
 */
public class MongoConfiguration {

  protected List<ServerAddress> serverAddresses = new ArrayList<>();
  protected String databaseName = "heisenberg";
  protected List<MongoCredential> credentials;
  protected MongoClientOptions.Builder optionBuilder = new MongoClientOptions.Builder();
  protected MongoProcessDefinitions.Fields processDefinitionFields;
  protected MongoProcessInstances.Fields processInstanceFields;
  protected WriteConcern writeConcernInsertProcessDefinition;
  protected WriteConcern writeConcernInsertProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;
  protected String processInstancesCollectionName = "processInstances";
  protected String processDefinitionsCollectionName = "processDefinitions";
  protected boolean isPretty = true;

  public MongoConfiguration server(String host, int port) {
    try {
      serverAddresses.add(new ServerAddress(host, port));
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public MongoConfiguration authentication(String userName, String database, char[] password) {
    if (credentials==null) {
      credentials = new ArrayList<>();
    }
    credentials.add(MongoCredential.createMongoCRCredential(userName, database, password));
    return this;
  }
  
  public MongoClientOptions.Builder getOptionBuilder() {
    return optionBuilder;
  }

  /** optional, if not set, {@ MongoProcessDefinitionMapper.Fields defaults} will be used */
  public void setProcessDefinitionFields(MongoProcessDefinitions.Fields processDefinitionFields) {
    this.processDefinitionFields = processDefinitionFields;
  }
  
  /** optional, if not set, {@ MongoProcessInstanceMapper.Fields defaults} will be used */
  public void setProcessInstanceFields(MongoProcessInstances.Fields processInstanceFields) {
    this.processInstanceFields = processInstanceFields;
  }

  public MongoProcessEngine buildProcessEngine() {
    return new MongoProcessEngine(this);
  }
  
  public MongoConfiguration writeConcernInsertProcessDefinition(WriteConcern writeConcernInsertProcessDefinition) {
    this.writeConcernInsertProcessDefinition = writeConcernInsertProcessDefinition;
    return this;
  }

  public MongoConfiguration writeConcernInsertProcessInstance(WriteConcern writeConcernInsertProcessInstance) {
    this.writeConcernInsertProcessInstance = writeConcernInsertProcessInstance;
    return this;
  }

  public MongoConfiguration writeConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
    return this;
  }
  
  public void processInstancesCollectionName(String processInstancesCollectionName) {
    this.processInstancesCollectionName = processInstancesCollectionName;
  }

  public void processDefinitionsCollectionName(String processDefinitionsCollectionName) {
    this.processDefinitionsCollectionName = processDefinitionsCollectionName;
  }
}

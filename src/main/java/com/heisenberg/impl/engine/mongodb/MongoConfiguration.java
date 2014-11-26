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
  protected ProcessDefinitionFieldNames processDefinitionFieldNames = new ProcessDefinitionFieldNames();
  protected ProcessInstanceFieldNames processInstanceFieldNames = new ProcessInstanceFieldNames();
  protected WriteConcern writeConcernStoreProcessDefinition;
  protected WriteConcern writeConcernStoreProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;

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
  
  public ProcessDefinitionFieldNames getProcessDefinitionFieldNames() {
    return processDefinitionFieldNames;
  }
  
  public ProcessInstanceFieldNames getProcessInstanceFieldNames() {
    return processInstanceFieldNames;
  }
  
  public MongoProcessEngine buildProcessEngine() {
    return new MongoProcessEngine(this);
  }
  
  public MongoConfiguration writeConcernStoreProcessDefinition(WriteConcern writeConcernStoreProcessDefinition) {
    this.writeConcernStoreProcessDefinition = writeConcernStoreProcessDefinition;
    return this;
  }

  public MongoConfiguration writeConcernStoreProcessInstance(WriteConcern writeConcernStoreProcessInstance) {
    this.writeConcernStoreProcessInstance = writeConcernStoreProcessInstance;
    return this;
  }

  public MongoConfiguration writeConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
    return this;
  }

  /** setting these fields to null will ensure those properties are not saved */
  public static class ProcessDefinitionFieldNames {
    public String _id = "_id";
    public String deployedTime = "dt";
    public String deployedBy = "db";
    public String organizationId = "o";
    public String processId = "p";
    public String version = "vn";
    public String activityDefinitions = "a";
    public String variableDefinitions = "v";
    public String transitionDefinitions = "t";
    public String activityTypeId = "ati";
    public String activityType = "at";
    public String dataTypeId = "yi";
    public String dataType = "y";
    public String initialValue = "i";
    public String from = "fr";
    public String to = "to";
  }

  public static class ProcessInstanceFieldNames {
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
}

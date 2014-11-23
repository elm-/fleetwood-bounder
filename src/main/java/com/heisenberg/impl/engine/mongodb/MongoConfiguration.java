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
  protected WriteConcern writeConcernStoreProcessDefinition;

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
  
  public MongoProcessEngine buildProcessEngine() {
    return new MongoProcessEngine(this);
  }

  /** setting these fields to null will ensure those properties are not saved */
  public static class ProcessDefinitionFieldNames {
    protected String _id = "_id";
    protected String name = "n";
    protected String deployedTime = "dt";
    protected String deployedBy = "db";
    protected String organizationId = "o";
    protected String processId = "p";
    protected String version = "v";
    protected String activityTypeId = "ati";
    protected String activityType = "at";
    protected String activityDefinitions = "a";
    protected String dataTypeId = "yi";
    protected String dataType = "y";
    protected String variableDefinitions = "v";
    protected String initialValue = "i";
    protected String from = "fr";
    protected String to = "to";
    protected String transitionDefinitions = "t";

    public MongoConfiguration.ProcessDefinitionFieldNames transitionDefinitions(String transitionDefinitions) {
      this.transitionDefinitions = transitionDefinitions;
      return this;
    }

    public MongoConfiguration.ProcessDefinitionFieldNames from(String from) {
      this.from = from;
      return this;
    }

    public MongoConfiguration.ProcessDefinitionFieldNames initialValue(String initialValue) {
      this.initialValue = initialValue;
      return this;
    }

    public MongoConfiguration.ProcessDefinitionFieldNames variableDefinitions(String variableDefinitions) {
      this.variableDefinitions = variableDefinitions;
      return this;
    }

    public ProcessDefinitionFieldNames _id(String _id) {
      this._id = _id;
      return this;
    }
    public ProcessDefinitionFieldNames name(String name) {
      this.name = name;
      return this;
    }
    public ProcessDefinitionFieldNames deployedTime(String deployedTime) {
      this.deployedTime = deployedTime;
      return this;
    }
    public ProcessDefinitionFieldNames deployedBy(String deployedBy) {
      this.deployedBy = deployedBy;
      return this;
    }
    public ProcessDefinitionFieldNames organizationId(String organizationId) {
      this.organizationId = organizationId;
      return this;
    }
    public ProcessDefinitionFieldNames processId(String processId) {
      this.processId = processId;
      return this;
    }
    public ProcessDefinitionFieldNames version(String version) {
      this.version = version;
      return this;
    }
    public ProcessDefinitionFieldNames activityTypeId(String activityTypeId) {
      this.activityTypeId = activityTypeId;
      return this;
    }
    public ProcessDefinitionFieldNames activityType(String activityType) {
      this.activityType = activityType;
      return this;
    }
    public ProcessDefinitionFieldNames activityDefinitions(String activityDefinitions) {
      this.activityDefinitions = activityDefinitions;
      return this;
    }
    public MongoConfiguration.ProcessDefinitionFieldNames dataType(String dataType) {
      this.dataType = dataType;
      return this;
    }
    public ProcessDefinitionFieldNames dataTypeId(String dataTypeId) {
      this.dataTypeId = dataTypeId;
      return this;
    }
  }
}

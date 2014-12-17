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
package com.heisenberg.api;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.configuration.ProcessEngineConfiguration;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.api.configuration.TaskService;
import com.heisenberg.impl.ExecutorService;
import com.heisenberg.impl.ProcessDefinitionCache;
import com.heisenberg.impl.engine.mongodb.MongoJobService;
import com.heisenberg.impl.engine.mongodb.MongoProcessEngine;
import com.heisenberg.impl.job.JobService;
import com.heisenberg.impl.util.Lists;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;


/**
 * @author Walter White
 */
public class MongoProcessEngineConfiguration extends ProcessEngineConfiguration {
  
  public static List<ServerAddress> DEFAULT_SERVER_ADDRESSES = Lists.of(createServerAddress("localhost", null));

  public static class ProcessDefinitionFields {
    public String _id = "_id";
    public String name = "name";
    public String deployedTime = "deployedTime";
    public String deployedBy = "deployedBy";
    public String organizationId = "oorganizationId";
    public String processId = "processId";
    public String version = "version";
    public String activityDefinitions = "activities";
    public String variableDefinitions = "variables";
    public String transitionDefinitions = "transitions";
    public String activityType = "activityType";
    public String dataType = "dataType";
    public String initialValue = "initialValue";
    public String from = "from";
    public String to = "to";
  }


  public static class ProcessInstanceFields {
    public String _id = "_id";
    public String organizationId = "organizationId";
    public String processDefinitionId = "processDefinitionId";
    public String start = "start";
    public String end = "end";
    public String duration = "duration";
    public String activityInstances = "activities";
    public String variableInstances = "variables";
    public String parent = "parent";
    public String variableDefinitionId = "variableDefinitionId";
    public String value = "value";
    public String activityDefinitionId = "activityDefinitionId";
    public String lock = "lock";
    public String time = "time";
    public String owner= "owner";
    public String updates = "updates";
    public String operations = "operations";
  }

  public static class JobFields {
    public String _id = "_id";
    public String key = "key";
    public String duedate = "duedate";
    public String lock = "lock";
    public String executions= "executions";
    public String retries = "retries";
    public String retryDelay = "retryDelay";
    public String done = "done";
    public String dead = "dead";
    public String organizationId = "organizationId";
    public String processId = "processId";
    public String processDefinitionId = "processDefinitionId";
    public String processInstanceId = "processInstanceId";
    public String lockProcessInstance = "lockProcessInstance";
    public String activityInstanceId = "activityInstanceId";
    public String taskId = "taskId";
    public String error = "error";
    public String logs = "logs";
    public String time = "time";
    public String duration = "duration";
    public String owner = "owner";
    public String jobType = "jobType";
  }
  
  protected List<ServerAddress> serverAddresses;
  protected String databaseName = "heisenberg";
  protected List<MongoCredential> credentials;
  protected MongoClientOptions.Builder optionBuilder = new MongoClientOptions.Builder();
  protected ProcessDefinitionFields processDefinitionFields;
  protected ProcessInstanceFields processInstanceFields;
  protected JobFields jobFields;
  protected WriteConcern writeConcernInsertProcessDefinition;
  protected WriteConcern writeConcernInsertProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;
  protected WriteConcern writeConcernJobs;
  protected String processInstancesCollectionName = "processInstances";
  protected String processDefinitionsCollectionName = "processDefinitions";
  protected String jobsCollectionName = "jobs";
  protected boolean isPretty = true;
  
  public ProcessEngine buildProcessEngine() {
    return new MongoProcessEngine(this);
  }
  
  public MongoProcessEngineConfiguration server(String host) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, null));
    return this;
  }

  public MongoProcessEngineConfiguration server(String host, int port) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, port));
    return this;
  }

  protected static ServerAddress createServerAddress(String host, Integer port) {
    try {
      if (port!=null) {
        return new ServerAddress(host, port);
      }
      return new ServerAddress(host);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public JobService createDefaultJobService() {
    return new MongoJobService();
  }

  public List<ServerAddress> getServerAddresses() {
    return serverAddresses!=null ? serverAddresses : DEFAULT_SERVER_ADDRESSES;
  }

  public MongoProcessEngineConfiguration authentication(String userName, String database, char[] password) {
    if (credentials==null) {
      credentials = new ArrayList<>();
    }
    credentials.add(MongoCredential.createMongoCRCredential(userName, database, password));
    return this;
  }
  
  public ProcessDefinitionFields getProcessDefinitionFields() {
    return processDefinitionFields!=null ? processDefinitionFields : new ProcessDefinitionFields();
  }
  
  public ProcessInstanceFields getProcessInstanceFields() {
    return processInstanceFields!=null ? processInstanceFields : new ProcessInstanceFields();
  }

  public JobFields getJobFields() {
    return jobFields!=null ? jobFields : new JobFields();
  }

  /** optional, if not set, {@ MongoProcessDefinitionMapper.Fields defaults} will be used */
  public void setProcessDefinitionFields(MongoProcessEngineConfiguration.ProcessDefinitionFields processDefinitionFields) {
    this.processDefinitionFields = processDefinitionFields;
  }
  
  /** optional, if not set, {@ MongoProcessInstanceMapper.Fields defaults} will be used */
  public void setProcessInstanceFields(MongoProcessEngineConfiguration.ProcessInstanceFields processInstanceFields) {
    this.processInstanceFields = processInstanceFields;
  }

  public MongoProcessEngineConfiguration writeConcernInsertProcessDefinition(WriteConcern writeConcernInsertProcessDefinition) {
    this.writeConcernInsertProcessDefinition = writeConcernInsertProcessDefinition;
    return this;
  }

  public MongoProcessEngineConfiguration writeConcernInsertProcessInstance(WriteConcern writeConcernInsertProcessInstance) {
    this.writeConcernInsertProcessInstance = writeConcernInsertProcessInstance;
    return this;
  }

  public MongoProcessEngineConfiguration writeConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
    return this;
  }
  
  public MongoProcessEngineConfiguration writeConcernJobs(WriteConcern writeConcernJobs) {
    this.writeConcernJobs = writeConcernJobs;
    return this;
  }
  
  public void processInstancesCollectionName(String processInstancesCollectionName) {
    this.processInstancesCollectionName = processInstancesCollectionName;
  }

  public void processDefinitionsCollectionName(String processDefinitionsCollectionName) {
    this.processDefinitionsCollectionName = processDefinitionsCollectionName;
  }

  public void jobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
  }

  @Override
  public MongoProcessEngineConfiguration id(String id) {
    super.id(id);
    return this;
  }

  @Override
  public MongoProcessEngineConfiguration processDefinitionCache(ProcessDefinitionCache processDefinitionCache) {
    super.processDefinitionCache(processDefinitionCache);
    return this;
  }

  @Override
  public MongoProcessEngineConfiguration jsonService(JsonService jsonService) {
    super.jsonService(jsonService);
    return this;
  }

  @Override
  public MongoProcessEngineConfiguration taskService(TaskService taskService) {
    super.taskService(taskService);
    return this;
  }

  @Override
  public MongoProcessEngineConfiguration scriptService(ScriptService scriptService) {
    super.scriptService(scriptService);
    return this;
  }

  @Override
  public MongoProcessEngineConfiguration executorService(ExecutorService executorService) {
    super.executorService(executorService);
    return this;
  }

  @Override
  public MongoProcessEngineConfiguration registerJavaBeanType(Class< ? > javaBeanClass) {
    super.registerJavaBeanType(javaBeanClass);
    return this;
  }

  public void setServerAddresses(List<ServerAddress> serverAddresses) {
    this.serverAddresses = serverAddresses;
  }

  
  public String getDatabaseName() {
    return databaseName;
  }
  
  public void setDatabaseName(String databaseName) {
    this.databaseName = databaseName;
  }
  
  public List<MongoCredential> getCredentials() {
    return credentials;
  }
  
  public void setCredentials(List<MongoCredential> credentials) {
    this.credentials = credentials;
  }

  public WriteConcern getWriteConcernInsertProcessDefinition() {
    return writeConcernInsertProcessDefinition;
  }
  
  public void setWriteConcernInsertProcessDefinition(WriteConcern writeConcernInsertProcessDefinition) {
    this.writeConcernInsertProcessDefinition = writeConcernInsertProcessDefinition;
  }
  
  public WriteConcern getWriteConcernInsertProcessInstance() {
    return writeConcernInsertProcessInstance;
  }
  
  public void setWriteConcernInsertProcessInstance(WriteConcern writeConcernInsertProcessInstance) {
    this.writeConcernInsertProcessInstance = writeConcernInsertProcessInstance;
  }
  
  public WriteConcern getWriteConcernJobs() {
    return writeConcernJobs;
  }

  public void setWriteConcernJobs(WriteConcern writeConcernJobs) {
    this.writeConcernJobs = writeConcernJobs;
  }

  public WriteConcern getWriteConcernFlushUpdates() {
    return writeConcernFlushUpdates;
  }
  
  public void setWriteConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
  }
  
  public String getProcessInstancesCollectionName() {
    return processInstancesCollectionName;
  }
  
  public void setProcessInstancesCollectionName(String processInstancesCollectionName) {
    this.processInstancesCollectionName = processInstancesCollectionName;
  }
 
  public String getProcessDefinitionsCollectionName() {
    return processDefinitionsCollectionName;
  }

  public void setProcessDefinitionsCollectionName(String processDefinitionsCollectionName) {
    this.processDefinitionsCollectionName = processDefinitionsCollectionName;
  }
  
  public String getJobsCollectionName() {
    return jobsCollectionName;
  }
  
  public void setJobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
  }

  public boolean isPretty() {
    return isPretty;
  }
  
  public void setPretty(boolean isPretty) {
    this.isPretty = isPretty;
  }
  
  public void setOptionBuilder(MongoClientOptions.Builder optionBuilder) {
    this.optionBuilder = optionBuilder;
  }

  public MongoClientOptions.Builder getOptionBuilder() {
    return optionBuilder;
  }
}

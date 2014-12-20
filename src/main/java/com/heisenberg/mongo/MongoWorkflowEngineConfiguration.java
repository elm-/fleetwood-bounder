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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.WorkflowEngineConfiguration;
import com.heisenberg.impl.job.JobType;
import com.heisenberg.impl.plugin.ActivityType;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.util.Lists;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;


/**
 * @author Walter White
 */
public class MongoWorkflowEngineConfiguration extends WorkflowEngineConfiguration {
  
  public static List<ServerAddress> DEFAULT_SERVER_ADDRESSES = Lists.of(createServerAddress("localhost", null));

  public static class WorkflowFields {
    public String _id = "_id";
    public String name = "name";
    public String deployedTime = "deployedTime";
    public String deployedBy = "deployedBy";
    public String organizationId = "oorganizationId";
    public String workflowId = "workflowId";
    public String version = "version";
    public String activitys = "activities";
    public String variables = "variables";
    public String transitions = "transitions";
    public String activityType = "activityType";
    public String dataType = "dataType";
    public String initialValue = "initialValue";
    public String from = "from";
    public String to = "to";
  }


  public static class WorkflowInstanceFields {
    public String _id = "_id";
    public String organizationId = "organizationId";
    public String workflowId = "workflowId";
    public String start = "start";
    public String end = "end";
    public String duration = "duration";
    public String activityInstances = "activities";
    public String archivedActivityInstances = "archivedActivities";
    public String variableInstances = "variables";
    public String parent = "parent";
    public String variableId = "variableId";
    public String value = "value";
    public String activityId = "activityId";
    public String lock = "lock";
    public String time = "time";
    public String owner= "owner";
    public String updates = "updates";
    public String workState = "workState";
    public String work = "work";
    public String asyncWork = "asyncWork";
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
    public String workflowId = "workflowId";
    public String workflowInstanceId = "workflowInstanceId";
    public String lockWorkflowInstance = "lockWorkflowInstance";
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
  protected WorkflowFields workflowFields;
  protected WorkflowInstanceFields workflowInstanceFields;
  protected JobFields jobFields;
  protected WriteConcern writeConcernInsertProcessDefinition;
  protected WriteConcern writeConcernInsertProcessInstance;
  protected WriteConcern writeConcernFlushUpdates;
  protected WriteConcern writeConcernJobs;
  protected String workflowInstancesCollectionName = "workflowInstances";
  protected String workflowsCollectionName = "workflowDefinitions";
  protected String jobsCollectionName = "jobs";
  protected boolean isPretty = true;
  
  public MongoWorkflowEngine buildProcessEngine() {
    return new MongoWorkflowEngine(this);
  }
  
  public MongoWorkflowEngineConfiguration server(String host) {
    if (serverAddresses==null) {
      serverAddresses = new ArrayList<>();
    }
    serverAddresses.add(createServerAddress(host, null));
    return this;
  }

  public MongoWorkflowEngineConfiguration server(String host, int port) {
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
  
  public List<ServerAddress> getServerAddresses() {
    return serverAddresses!=null ? serverAddresses : DEFAULT_SERVER_ADDRESSES;
  }

  public MongoWorkflowEngineConfiguration authentication(String userName, String database, char[] password) {
    if (credentials==null) {
      credentials = new ArrayList<>();
    }
    credentials.add(MongoCredential.createMongoCRCredential(userName, database, password));
    return this;
  }
  
  public WorkflowFields getProcessDefinitionFields() {
    return workflowFields!=null ? workflowFields : new WorkflowFields();
  }
  
  public WorkflowInstanceFields getProcessInstanceFields() {
    return workflowInstanceFields!=null ? workflowInstanceFields : new WorkflowInstanceFields();
  }

  public JobFields getJobFields() {
    return jobFields!=null ? jobFields : new JobFields();
  }

  /** optional, if not set, {@ MongoProcessDefinitionMapper.Fields defaults} will be used */
  public void setProcessDefinitionFields(MongoWorkflowEngineConfiguration.WorkflowFields workflowFields) {
    this.workflowFields = workflowFields;
  }
  
  /** optional, if not set, {@ MongoProcessInstanceMapper.Fields defaults} will be used */
  public void setProcessInstanceFields(MongoWorkflowEngineConfiguration.WorkflowInstanceFields workflowInstanceFields) {
    this.workflowInstanceFields = workflowInstanceFields;
  }

  public MongoWorkflowEngineConfiguration writeConcernInsertProcessDefinition(WriteConcern writeConcernInsertProcessDefinition) {
    this.writeConcernInsertProcessDefinition = writeConcernInsertProcessDefinition;
    return this;
  }

  public MongoWorkflowEngineConfiguration writeConcernInsertProcessInstance(WriteConcern writeConcernInsertProcessInstance) {
    this.writeConcernInsertProcessInstance = writeConcernInsertProcessInstance;
    return this;
  }

  public MongoWorkflowEngineConfiguration writeConcernFlushUpdates(WriteConcern writeConcernFlushUpdates) {
    this.writeConcernFlushUpdates = writeConcernFlushUpdates;
    return this;
  }
  
  public MongoWorkflowEngineConfiguration writeConcernJobs(WriteConcern writeConcernJobs) {
    this.writeConcernJobs = writeConcernJobs;
    return this;
  }
  
  public void processInstancesCollectionName(String processInstancesCollectionName) {
    this.workflowInstancesCollectionName = processInstancesCollectionName;
  }

  public void processDefinitionsCollectionName(String processDefinitionsCollectionName) {
    this.workflowsCollectionName = processDefinitionsCollectionName;
  }

  public void jobsCollectionName(String jobsCollectionName) {
    this.jobsCollectionName = jobsCollectionName;
  }

  @Override
  public MongoWorkflowEngineConfiguration id(String id) {
    super.id(id);
    return this;
  }
  
  @Override
  public MongoWorkflowEngineConfiguration registerService(Object service) {
    super.registerService(service);
    return this;
  }

  @Override
  public MongoWorkflowEngineConfiguration registerJavaBeanType(Class< ? > javaBeanType) {
    super.registerJavaBeanType(javaBeanType);
    return this;
  }

  @Override
  public MongoWorkflowEngineConfiguration registerActivityType(ActivityType activityType) {
    super.registerActivityType(activityType);
    return this;
  }

  @Override
  public MongoWorkflowEngineConfiguration registerDataType(DataType dataType) {
    super.registerDataType(dataType);
    return this;
  }
  
  @Override
  public MongoWorkflowEngineConfiguration registerJobType(Class< ? extends JobType> jobTypeClass) {
    super.registerJobType(jobTypeClass);
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
  
  public String getWorkflowInstancesCollectionName() {
    return workflowInstancesCollectionName;
  }
  
  public void setWorkflowInstancesCollectionName(String processInstancesCollectionName) {
    this.workflowInstancesCollectionName = processInstancesCollectionName;
  }
 
  public String getWorkflowsCollectionName() {
    return workflowsCollectionName;
  }

  public void setWorkflowsCollectionName(String processDefinitionsCollectionName) {
    this.workflowsCollectionName = processDefinitionsCollectionName;
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

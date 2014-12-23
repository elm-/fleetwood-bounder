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
package com.heisenberg.test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.definition.Workflow;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.api.task.Task;
import com.heisenberg.api.task.TaskService;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.job.Job;
import com.heisenberg.impl.job.JobService;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.plugin.ServiceRegistry;
import com.heisenberg.mongo.MongoWorkflowEngineConfiguration;


/** Base class that allows to reuse tests and run them on different process engines.
 *  
 * @author Walter White
 */
public class WorkflowTest {
  
  public static final Logger log = LoggerFactory.getLogger(WorkflowTest.class);
  
  protected static final String MEMORY = "memory";
  protected static final String MONGO = "mongo";

  protected WorkflowEngine workflowEngine = null;
  protected String workflowEngineId = MEMORY;
  
  protected void useMongoWorkflowEngine() {
    workflowEngineId = MONGO;
  }
  
  @Before
  public void before() {
    if (MEMORY.equals(workflowEngineId)) {
      workflowEngine = new TestMemoryWorkflowEngine();

    } else if (MONGO.equals(workflowEngineId)) {
      workflowEngine = new MongoWorkflowEngineConfiguration()
        .registerService(new TestExecutorService())
        .buildProcessEngine();
      TestHelper.mongoDeleteAllCollections(workflowEngine);
      
    } else {
      throw new RuntimeException("Invalid workflow engine id: "+workflowEngineId);
    }
  }
  
  @After
  public void after() {
    if (MONGO.equals(workflowEngineId)) {
      cleanAndLogMongoContent();
    }
  }

  protected void cleanAndLogMongoContent() {
    log.debug("\n\n###### Test ended, starting cleanup ######################################################## \n");

    StringBuilder cleanLog = new StringBuilder();
    cleanLog.append("Cleaning up workflow engine\n");

    ServiceRegistry serviceRegistry = ((WorkflowEngineImpl) workflowEngine).getServiceRegistry();
    JsonService jsonService = serviceRegistry.getService(JsonService.class);
    JobService jobService = serviceRegistry.getService(JobService.class);
    TaskService taskService = serviceRegistry.getService(TaskService.class);

    List<Job> jobs = jobService.newJobQuery().asList();
    if (jobs != null && !jobs.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### jobs ######################################################## \n");
      for (Job job : jobs) {
        jobService.deleteJob(job.getId());
        cleanLog.append("--- Deleted job ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(job));
        cleanLog.append("\n");
        i++;
      }
    }
    List<Task> tasks = taskService.newTaskQuery().asList();
    if (tasks != null && !tasks.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### tasks ######################################################## \n");
      for (Task task : tasks) {
        taskService.deleteTask(task.getId());
        cleanLog.append("--- Deleted task ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(task));
        cleanLog.append("\n");
        i++;
      }
    }
    List< ? extends WorkflowInstance> workflowInstances = workflowEngine.newWorkflowInstanceQuery().asList();
    if (workflowInstances != null && !workflowInstances.isEmpty()) {
      int i = 0;
      cleanLog.append("\n\n### workflowInstances ################################################ \n");
      for (WorkflowInstance workflowInstance : workflowInstances) {
        workflowEngine.deleteWorkflowInstance(workflowInstance.getId());
        cleanLog.append("--- Deleted workflow instance ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(workflowInstance));
        cleanLog.append("\n");
        i++;
      }
    }
    List< ? extends Workflow> workflows = workflowEngine.newWorkflowQuery().asList();
    if (workflows != null && !workflows.isEmpty()) {
      int i = 0;
      cleanLog.append("\n### workflows ######################################################## \n");
      for (Workflow workflow : workflows) {
        workflowEngine.deleteWorkflow(workflow.getId());
        cleanLog.append("--- Deleted workflow ");
        cleanLog.append(i);
        cleanLog.append(" ---\n");
        cleanLog.append(jsonService.objectToJsonStringPretty(workflow));
        cleanLog.append("\n");
        i++;
      }
    }
    log.debug(cleanLog.toString());
  }
}

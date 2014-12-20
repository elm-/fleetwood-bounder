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
package com.heisenberg.test.other;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.activitytypes.UserTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.impl.ExecutorService;
import com.heisenberg.impl.WorkflowEngineImpl;
import com.heisenberg.impl.Time;
import com.heisenberg.impl.job.AbstractJobType;
import com.heisenberg.impl.job.JobController;
import com.heisenberg.impl.job.JobExecution;
import com.heisenberg.impl.job.JobService;
import com.heisenberg.impl.job.JobServiceImpl;
import com.heisenberg.impl.memory.MemoryWorkflowEngine;


/**
 * @author Walter White
 */
public class JobServiceTest {
  
  public static final Logger log = LoggerFactory.getLogger(JobServiceTest.class);

  protected WorkflowEngineImpl processEngine;
  protected JobService jobService;

  public JobServiceTest() {
    initialize();
  }

  public void initialize() {
    initializeProcessEngine();
    this.jobService = processEngine.getServiceRegistry().getService(JobService.class);
    JobServiceImpl jobServiceImpl = (JobServiceImpl) jobService;
    // let's skip starting any threads
    jobServiceImpl.isRunning = true;
    // and execute the jobs in the test thread..
    jobServiceImpl.executor = new ExecutorService() {
      public void startup() {}
      public void shutdown() {}
      public int getQueueDepth() {return 0;}
      public void execute(Runnable command) {
        command.run();
      }
    };
  }

  public void initializeProcessEngine() {
    this.processEngine = new MemoryWorkflowEngine();
  }
  
  @JsonTypeName("tst")
  public static class TestJob extends AbstractJobType {
    static List<JobExecution> jobExecutions;
    static boolean throwException;
    @Override
    public void execute(JobController jobController) {
      jobExecutions.add((JobExecution)jobController);
      if (throwException) {
        jobController.log("oops");
        throw new RuntimeException("oops");
      } else {
        jobController.log(":ok_hand:");
      }
    }
  }
  
  @Before
  public void before() {
    TestJob.jobExecutions = new ArrayList<>();
    TestJob.throwException = false;
    Time.now = null;
  }
  
  @Test
  public void testProcessJobOK() throws Exception {
    // quickest way to get a processInstanceId
    WorkflowBuilder p = processEngine.newWorkflow();
    p.newActivity("t", new UserTask());
    String processDefinitionId = p.deploy().getWorkflowId();
    String processInstanceId = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance()
      .getId();
    
    jobService.newJob(new TestJob())
      .duedate(Time.now())
      .processInstanceId(processInstanceId)
      .save();
    
    assertEquals(0, TestJob.jobExecutions.size());
    checkOtherJobs(); 
    assertEquals(0, TestJob.jobExecutions.size());
    checkProcessJobs(); // only this one should execute the job
    assertEquals(1, TestJob.jobExecutions.size());
    checkOtherJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    checkProcessJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    
    JobExecution jobExecution = TestJob.jobExecutions.get(0);
    assertNull(jobExecution.error);
  }

  @Test
  public void testRealProcessJobOK() throws Exception {
    // quickest way to get a processInstanceId
    WorkflowBuilder p = processEngine.newWorkflow();
    p.newActivity("t", new UserTask())
     .newTimer(new TestJob())
     ;
    String processDefinitionId = p.deploy().getWorkflowId();
    String processInstanceId = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .startProcessInstance()
      .getId();
    
    jobService.newJob(new TestJob())
      .duedate(Time.now())
      .processInstanceId(processInstanceId)
      .save();
    
    assertEquals(0, TestJob.jobExecutions.size());
    checkOtherJobs(); 
    assertEquals(0, TestJob.jobExecutions.size());
    checkProcessJobs(); // only this one should execute the job
    assertEquals(1, TestJob.jobExecutions.size());
    checkOtherJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    checkProcessJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    
    JobExecution jobExecution = TestJob.jobExecutions.get(0);
    assertNull(jobExecution.error);
  }


  @Test
  public void testOtherJobOK() throws Exception {
    jobService.newJob(new TestJob())
      .duedate(Time.now())
      .save();
    
    checkProcessJobs();
    assertEquals(0, TestJob.jobExecutions.size());
    checkOtherJobs();  // only this one should execute the job
    assertEquals(1, TestJob.jobExecutions.size());
    checkOtherJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    checkProcessJobs();
    assertEquals(1, TestJob.jobExecutions.size());

    JobExecution jobExecution = TestJob.jobExecutions.get(0);
    assertNull(jobExecution.error);
  }

  @Test
  public void testOtherJobFailAndRecover() throws Exception {
    jobService.newJob(new TestJob())
      .duedate(Time.now())
      .save();
    
    TestJob.throwException = true;
    
    assertEquals(0, TestJob.jobExecutions.size());
    checkOtherJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    
    assertTrue(TestJob.jobExecutions.get(0).error);
    assertNull(TestJob.jobExecutions.get(0).job.done);

    // the default retry reschedule is 3 seconds
    Time.now = new LocalDateTime().plusMinutes(10);
    TestJob.throwException = false;
    
    checkOtherJobs();
    assertEquals(2, TestJob.jobExecutions.size());
    
    assertNull(TestJob.jobExecutions.get(1).error);
    assertNotNull(TestJob.jobExecutions.get(1).job.done);
  }
  
  @Test
  public void testOtherJobFailTillDead() throws Exception {
    jobService.newJob(new TestJob())
      .duedate(Time.now())
      .save();
    
    TestJob.throwException = true;
    
    checkOtherJobs();
    assertEquals(1, TestJob.jobExecutions.size());
    JobExecution jobExecution = TestJob.jobExecutions.get(0);
    assertTrue(jobExecution.error);
    assertNull(jobExecution.job.done);
    assertNull(jobExecution.job.dead);

    // the first retry rescheduled in 3 seconds
    Time.now = new LocalDateTime().plusSeconds(4);

    checkOtherJobs();
    assertEquals(2, TestJob.jobExecutions.size());
    jobExecution = TestJob.jobExecutions.get(1);
    assertTrue(jobExecution.error);
    assertNull(jobExecution.job.done);
    assertNull(jobExecution.job.dead);

    // the second retry rescheduled in an hour seconds
    Time.now = new LocalDateTime().plusHours(2);

    checkOtherJobs();
    assertEquals(3, TestJob.jobExecutions.size());
    jobExecution = TestJob.jobExecutions.get(2);
    assertTrue(jobExecution.error);
    assertNull(jobExecution.job.done);
    assertNull(jobExecution.job.dead);

    // the second retry rescheduled in an hour seconds
    Time.now = new LocalDateTime().plusDays(2);

    checkOtherJobs();
    assertEquals(4, TestJob.jobExecutions.size());
    jobExecution = TestJob.jobExecutions.get(3);
    assertTrue(jobExecution.error);
    
    // But now the job should be dead 
    assertNotNull(jobExecution.job.done);
    assertTrue(jobExecution.job.dead);
  }

  @Test
  public void testUniqueJob() throws Exception {
    jobService.newJob(new TestJob())
      .key("uniqueid")
      .save();
  
    jobService.newJob(new TestJob())
      .key("uniqueid")
      .save();
  
    checkOtherJobs();
    assertEquals(1, TestJob.jobExecutions.size());
  }


  public void checkProcessJobs() {
    ((JobServiceImpl)jobService).checkProcessJobs();
  }

  public void checkOtherJobs() {
    ((JobServiceImpl)jobService).checkOtherJobs();
  }
}

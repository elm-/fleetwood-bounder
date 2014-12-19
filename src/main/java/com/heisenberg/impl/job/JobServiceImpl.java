/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.heisenberg.impl.job;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import com.heisenberg.impl.ExecutorService;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQueryImpl;
import com.heisenberg.impl.WorkflowInstanceStore;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.plugin.ServiceRegistry;


/**
 * @author Tom Baeyens
 */
public abstract class JobServiceImpl implements JobService {
  
  // private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
  
  public ProcessEngineImpl processEngine;
  public ExecutorService executor = null;

  // configuration 
  public long checkInterval = 30 * 1000; // 30 seconds
  public int maxJobExecutions = 5;

  // runtime state
  public boolean isRunning = false;
  public Timer timer = null;
  public Timer checkOtherJobsTimer = null;
  public JobServiceListener listener = null;
  
  public JobServiceImpl() {
  }

  
  public JobServiceImpl(ServiceRegistry serviceRegistry) {
    this.processEngine = serviceRegistry.getService(ProcessEngineImpl.class);
    this.executor = serviceRegistry.getService(ExecutorService.class);
  }

  public Job newJob(JobType jobType) {
    return new Job(this, jobType);
  }

  public void setListener(JobServiceListener listener) {
    this.listener = listener;
  }

  public synchronized void startup() {
    if (!isRunning) {
      if (executor==null) {
        throw new RuntimeException("No executor configured in JobExecutor");
      }
      
      timer = new Timer("Job executor timer");

      keepDoing(new Runnable() {
        @Override
        public void run() {
          checkProcessJobs();
        }
      }, 100, checkInterval);

      keepDoing(new Runnable() {
        @Override
        public void run() {
          checkOtherJobs();
        }
      }, 500, checkInterval);

      isRunning = true;
    }
  }
  
  /** Repeatedly executes the given doable until this job executor is shutdown.
   * It uses the process engine executor service to execute the doable.
   * We use a single timer object.  As each timer uses it's own thread, we ensure 
   * that the job executor only uses 1 thread for triggering all the recurring 
   * method invocations.  We delegate the doables to the executor, so we're sure they 
   * won't take long. */
  public void keepDoing(final Runnable doable, long delay, long period) {
    timer.schedule(new TimerTask(){
      @Override
      public void run() {
        executor.execute(doable);
      }
    }, delay, period);
  }

  public void shutdown() {
    timer.cancel();
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void checkProcessJobs() {
    WorkflowInstanceStore workflowInstanceStore = processEngine.getWorkflowInstanceStore();
    Iterator<String> processInstanceIds = getProcessInstanceIdsToLockForJobs();
    while (isRunning 
           && processInstanceIds!=null 
           && processInstanceIds.hasNext()) {
      String processInstanceId = processInstanceIds.next();
      ProcessInstanceQueryImpl query = processEngine.newProcessInstanceQuery()
        .processInstanceId(processInstanceId);
      ProcessInstanceImpl lockedProcessInstance = workflowInstanceStore.lockProcessInstance(query);
      boolean keepGoing = true;
      while (isRunning && keepGoing) {
        Job job = lockNextProcessJob(processInstanceId);
        if (job != null) {
          executor.execute(new ExecuteJob(job, lockedProcessInstance));
        } else {
          keepGoing = false;
        }
      }
      workflowInstanceStore.flushAndUnlock(lockedProcessInstance);
    }
  }
  
  public void checkOtherJobs() {
    boolean keepGoing = true;
    while (isRunning && keepGoing) {
      Job job = lockNextOtherJob();
      if (job != null) {
        executor.execute(new ExecuteJob(job));
      } else {
        keepGoing = false;
      }
    }
  }
  
  class ExecuteJob implements Runnable {
    JobServiceImpl jobService;
    Job job;
    ProcessInstanceImpl processInstance;
    public ExecuteJob(Job job) {
      this.job = job;
    }
    public ExecuteJob(Job job, ProcessInstanceImpl processInstance) {
      this.job = job;
      this.processInstance = processInstance;
    }
    @Override
    public void run() {
      executeJob(job, processInstance);
    }
  }
  
  public void executeJob(Job job, ProcessInstanceImpl processInstance) {
    JobExecution jobExecution = new JobExecution(job, processInstance);
    job.duedate = null;
    job.lock = null;
    JobType jobType = job.jobType;
    try {
      try {
        jobType.execute(jobExecution);
        if (job.duedate==null) { // if reschedule() was not called...
          job.done = new LocalDateTime();
          if (listener!=null) {
            listener.notifyJobDone(jobExecution);
          }
        } else {
          if (listener!=null) {
            listener.notifyJobRescheduled(jobExecution);
          }
        }
        
      } catch (Throwable exception) {
        StringWriter stackTraceCollector = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTraceCollector));
        jobExecution.log(stackTraceCollector.toString());
        jobExecution.error = true;
        if (job.retries==null) {
          job.retries = (long) jobType.getMaxRetries();
        } 
        if (job.retries>0) {
          job.retries--;
          long retry = jobType.getMaxRetries()-job.retries;
          int delayInSeconds = jobType.getRetryDelayInSeconds(retry);
          jobExecution.rescheduleFromNow(Seconds.seconds((int)delayInSeconds));
          if (listener!=null) {
            listener.notifyJobRetry(jobExecution);
          }
        } else {
          // ALARM !  Manual intervention required
          job.done = new LocalDateTime();
          job.dead = true;
          if (listener!=null) {
            listener.notifyJobFailure(jobExecution);
          }
        }
      }
    } finally {
      if (job.executions==null) {
        job.executions = new LinkedList<>();
      }
      job.executions.add(jobExecution);
      if (job.executions.size()>maxJobExecutions) {
        job.executions.remove(0);
      }
      saveJob(job);
    }
  }
  
  /** returns the ids of process instance that have jobs requiring
   * a process instance lock. When a job requires a process instance lock, 
   * it has to specify {@link Job#processInstanceId} and set {@link Job#lockProcessInstance} to true.
   * This method is allowed to return null. */
  public abstract Iterator<String> getProcessInstanceIdsToLockForJobs();

  /** locks a job having the given processInstanceId and retrieves it from the store */
  public abstract Job lockNextProcessJob(String processInstanceId);

  /** locks a job not having a {@link Job#lockProcessInstance} specified  
   * and retrieves it from the store */
  public abstract Job lockNextOtherJob();

  public abstract void saveJob(Job job);
}

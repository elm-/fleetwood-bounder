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
import java.util.concurrent.Executor;

import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQueryImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;


/**
 * @author Tom Baeyens
 */
public abstract class JobServiceImpl implements JobService {
  
  // private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
  
  public ProcessEngineImpl processEngine;

  // configuration 
  public long checkInterval = 30 * 1000; // 30 seconds
  public int maxJobExecutions = 5;

  // runtime state
  public boolean isRunning = false;
  public Executor executor = null;
  public Timer timer = null;
  public Timer checkOtherJobsTimer = null;
  public JobServiceListener listener = null;
  
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }
  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
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
    Iterator<String> processInstanceIds = getProcessInstanceIdsToLockForJobs();
    while (isRunning && processInstanceIds.hasNext()) {
      String processInstanceId = processInstanceIds.next();
      ProcessInstanceQueryImpl query = processEngine.newProcessInstanceQuery()
        .id(processInstanceId);
      ProcessInstanceImpl lockedProcessInstance = processEngine.lockProcessInstance(query);
      boolean keepGoing = true;
      while (isRunning && keepGoing) {
        Job job = lockNextProcessJob(processInstanceId);
        if (job != null) {
          executor.execute(new ExecuteJob(job, lockedProcessInstance));
        } else {
          keepGoing = false;
        }
      }
      processEngine.flushAndUnlock(lockedProcessInstance);
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
    JobExecution jobExecution = new JobExecution(processInstance);
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
          job.retries = jobType.getMaxRetries();
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
      updateJob(job);
    }
  }
  
  public void ensureJob(Class<? extends JobType> jobType) {
    ensureJob(jobType, null);
  }
  
  /** Ensures that one job with the given type exists in the database.<br> 
   * If none exists it will be created and scheduled to the given time (firstStart).
   * If the given time is null, the job will be scheduled to run shortly after the creation.
   * 
   * @param jobType - the job to be scheduled
   * @param firstStart - (optional) the first time the job should be executed 
   */
  public void ensureJob(Class<? extends JobType> jobType, LocalDateTime firstStart) {
    Job job = new Job();
    try {
      job.jobType = jobType.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate job "+jobType);
    }
    if (firstStart != null) {
      job.rescheduleFor(firstStart);
    } else {
      job.rescheduleFromNow(Seconds.seconds(10));
    }
    ensureJob(job);
  }

  /** returns the ids of process instance that have jobs requiring
   * a process instance lock. When a job requires a process instance lock, 
   * it has to specify {@link Job#processInstanceId} and set {@link Job#lockProcessInstance} to true. */
  public abstract Iterator<String> getProcessInstanceIdsToLockForJobs();

  /** inserts the job only if no job with the given job.jobType exists */
  public abstract void ensureJob(Job job);

  /** locks a job having the given processInstanceId and retrieves it from the store */
  public abstract Job lockNextProcessJob(String processInstanceId);

  /** locks a job not having a {@link Job#lockProcessInstance} specified  
   * and retrieves it from the store */
  public abstract Job lockNextOtherJob();

  public abstract void insertJob(Job job);

  public abstract void updateJob(Job job);
}

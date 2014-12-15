/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.heisenberg.impl.job;

import java.util.LinkedList;

import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePeriod;


/**
 * @author Tom Baeyens
 */
public class Job {
  
  // private static final Logger log = LoggerFactory.getLogger(Job.class);

  public JobType jobType;
  
  public LocalDateTime duedate;
  public Lock lock;
  public LinkedList<JobExecution> executions;
  /** retries left.  null when no retries have been performed. 0 when this job has permanently failed. */
  public Long retries;
  public Long retryDelay;
  public LocalDateTime done;

  public String organizationId;
  public String processId;
  public String processDefinitionId;
  public String processInstanceId;
  public Boolean lockProcessInstance;
  public String activityInstanceId;
  public String taskId;

  public Job activityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }
  
  public Job done(LocalDateTime done) {
    this.done = done;
    return this;
  }
  
  public Job duedate(LocalDateTime duedate) {
    this.duedate = duedate;
    return this;
  }
  
  public Job lock(Lock lock) {
    this.lock = lock;
    return this;
  }
  
  public Job organizationId(String organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public Job processId(String processId) {
    this.processId = processId;
    return this;
  }
  
  public Job processDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }
  
  public Job processInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
    return this;
  }
  
  public Job taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }
  
  public Job jobType(JobType jobType) {
    this.jobType = jobType;
    return this;
  }

  public void rescheduleFromNow(ReadablePeriod period) {
    rescheduleFor(new LocalDateTime().plus(period));
  }

  public void rescheduleFor(LocalDateTime duedate) {
    this.duedate = duedate;
  }
}
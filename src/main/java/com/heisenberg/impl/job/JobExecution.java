/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.heisenberg.impl.job;

import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePeriod;

import com.heisenberg.impl.instance.ProcessInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class JobExecution implements JobController {
  
  public Job job;
  public ProcessInstanceImpl processInstance;
  public Boolean error;
  public String logs;

  public JobExecution() {
  }

  public JobExecution(ProcessInstanceImpl processInstance) {
    this.processInstance = processInstance;
    this.time = new LocalDateTime();
  }

  public LocalDateTime time;

  public void rescheduleFromNow(ReadablePeriod period) {
    job.rescheduleFromNow(period);
  }
  
  public void rescheduleFor(LocalDateTime duedate) {
    job.rescheduleFor(duedate);
  }
  
  public void log(String msg) {
    logs = (logs!=null ? logs : "") + msg + "\n";
  }

  public ProcessInstanceImpl getProcessInstance() {
    return processInstance;
  }
}

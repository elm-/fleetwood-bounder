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
package com.heisenberg.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.heisenberg.impl.Time;
import com.heisenberg.impl.job.Job;
import com.heisenberg.impl.job.JobServiceImpl;


/**
 * @author Walter White
 */
public class MemoryJobServiceImpl extends JobServiceImpl {
  
  Set<String> processInstanceIds = new HashSet<>();
  LinkedList<Job> jobs = new LinkedList<>();
  List<Job> jobsDone = new ArrayList<>();
  Map<String,Job> jobsById = new HashMap<>();

  @Override
  public synchronized Iterator<String> getProcessInstanceIdsToLockForJobs() {
    return processInstanceIds.iterator();
  }

  @Override
  public synchronized Job lockNextProcessJob(String processInstanceId) {
    return lockJob(true);
  }

  @Override
  public synchronized Job lockNextOtherJob() {
    return lockJob(false);
  }

  public synchronized Job lockJob(boolean mustHaveProcessInstance) {
    Iterator<Job> jobIterator = jobs.iterator();
    while (jobIterator.hasNext()) {
      Job job = jobIterator.next();
      if ( ( job.duedate==null || duedateHasPast(job) )
           && ( (mustHaveProcessInstance && job.processInstanceId!=null)
                || (!mustHaveProcessInstance && job.processInstanceId==null) )
         ) {
        jobIterator.remove();
        return job;
      }
    }
    return null;
  }

  public boolean duedateHasPast(Job job) {
    long nowMillis = Time.now().toDate().getTime();
    long duedateMillis = job.duedate.toDate().getTime();
    return (duedateMillis - nowMillis)<=0;
  }

  @Override
  public synchronized void saveJob(Job job) {
    if (job.done==null) {
      jobs.add(job);
      if (job.key!=null) {
        Job oldJob = jobsById.put(job.key, job);
        if (oldJob!=null) {
          jobs.remove(oldJob);
        }
      }
      if (job.processInstanceId != null) {
        processInstanceIds.add(job.processInstanceId);
      }
    } else {
      jobsDone.add(job);
    }
  }
}

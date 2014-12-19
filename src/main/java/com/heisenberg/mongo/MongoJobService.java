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

import java.util.Iterator;

import com.heisenberg.impl.job.Job;
import com.heisenberg.impl.job.JobService;
import com.heisenberg.impl.job.JobServiceImpl;
import com.heisenberg.impl.job.JobType;
import com.heisenberg.plugin.ServiceRegistry;



/**
 * @author Walter White
 */
public class MongoJobService extends JobServiceImpl implements JobService {
  
  protected MongoJobs jobs;

  public MongoJobService() {
  }

  public MongoJobService(ServiceRegistry serviceRegistry) {
    super(serviceRegistry);
    this.jobs = serviceRegistry.getService(MongoJobs.class);
  }

  @Override
  public Job newJob(JobType jobType) {
    return new Job(this, jobType);
  }

  @Override
  public Iterator<String> getProcessInstanceIdsToLockForJobs() {
    return jobs.getProcessInstanceIdsToLockForJobs();
  }

  public Job lockNextProcessJob(String processInstanceId) {
    return jobs.lockJob(true);
  }

  @Override
  public Job lockNextOtherJob() {
    return jobs.lockJob(false);
  }

  @Override
  public void saveJob(Job job) {
    jobs.saveJob(job);
  }
}

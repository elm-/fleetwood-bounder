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
package com.heisenberg.impl.engine.mongodb;

import com.heisenberg.impl.job.JobService;



/**
 * @author Walter White
 */
public class MongoJobService implements JobService {

//  job fields
//  public static final String DB_activityInstanceId = "ai";
//  public static final String DB_done = "d";
//  public static final String DB_duedate = "dd";
//  public static final String DB_executions = "execs";
//  public static final String DB_lock = "l";
//  public static final String DB_organizationId = "oz";
//  public static final String DB_processId = "pr";
//  public static final String DB_processDefinitionId = "pd";
//  public static final String DB_processInstanceId = "pi";
//  public static final String DB_retries = "retries";
//  public static final String DB_taskId = "t";
//
//  job execution fields
//  public static final String DB_error = "error";
//  public static final String DB_logs = "logs";
//  public static final String DB_time = "time";
//
//
//  @Override
//  protected Job lockNextJob() {
//    return null;
//  }
//
//  protected Query<Job> createJobLockQuery() {
//    return Job.createQuery()
//            .field(Job.DB_duedate).lessThanOrEq(new LocalDateTime())
//            .field(Job.DB_lock).doesNotExist()
//            .field(Job.DB_done).doesNotExist();
//  }
//
//  public abstract void schedule(Job job) {
//    if (job.duedate==null) {
//      throw new RuntimeException("job.duedate is null");
//    }
//    if (job.lock!=null) {
//      throw new RuntimeException("job.lock is not null");
//    }
//    tx.addSave(job);
//  }
//  public static Query<Job> createQuery() {
//    return Db9.getDatastore().createQuery(Job.class);
//  }
//  
//  public static Query<Job> createQuery(ObjectId jobId) {
//    return createQuery().field(DB_id).equal(jobId);
//  }
//
//  public static UpdateOperations<Job> createUpdate() {
//    return Db9.createUpdateOperations(Job.class);
//  }
//  
//  /**
//   * Ensures that one job with the given type exists in the database.
//   * 
//   * @param jobType - the job to be scheduled
//   */
//  public static void ensureJob(Class<? extends Job> jobType) {
//    ensureJob(jobType, null);
//  }
//  
//  /**
//   * Ensures that one job with the given type exists in the database.<br> 
//   * If none exists it will be created and scheduled to the given time (firstStart).
//   * If the given time is null, the job will be scheduled to run shortly after the creation.
//   * 
//   * @param jobType - the job to be scheduled
//   * @param firstStart - (optional) the first time the job should be executed 
//   */
//  @Override
//  public static void ensureJob(Class<? extends Job> jobType, LocalDateTime firstStart) {
//    if (Db9.createQuery(jobType)
//           .disableValidation()
//           .field("className").equal(jobType.getName())
//           .countAll()==0) {
//      try {
//        Job job = jobType.newInstance();
//        if (firstStart != null) {
//          job.rescheduleFor(firstStart);
//        } else {
//          job.rescheduleFromNow(Seconds.seconds(10));
//        }
//        // job.rescheduleFromNow(Minutes.minutes(5));
//        Db9.save(job);
//      } catch (Exception e) {
//        log.error("Initialization bug: "+e.getMessage(), e);
//      }
//    }
//  }
}

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
package com.heisenberg.impl.engine.memory;

import java.util.Iterator;

import com.heisenberg.impl.job.Job;
import com.heisenberg.impl.job.JobServiceImpl;


/**
 * @author Walter White
 */
public class MemoryJobServiceImpl extends JobServiceImpl {

  @Override
  public Iterator<String> getProcessInstanceIdsToLockForJobs() {
    return null;
  }

  @Override
  public void ensureJob(Job job) {
  }

  @Override
  public Job lockNextProcessJob(String processInstanceId) {
    return null;
  }

  @Override
  public Job lockNextOtherJob() {
    return null;
  }

  @Override
  public void insertJob(Job job) {
  }

  @Override
  public void updateJob(Job job) {
  }

}

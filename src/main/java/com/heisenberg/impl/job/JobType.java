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
package com.heisenberg.impl.job;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/** Use {@link AbstractJobType} to leverage the default 
 * retry strategy.
 * 
 * @author Walter White
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type")
public interface JobType {

  /** one less then the total number of attempts before giving up.
   * So returning 0 means only 1 attempt is done and no retries.  */
  int getMaxRetries();
  
  /** @param retry indicates the sequence number for the retry to be 
   * scheduled for the job to implement incremental backoff, so it 
   * will be 1 the first time. */
  int getRetryDelayInSeconds(long retry);

  void execute(JobController jobController);
}

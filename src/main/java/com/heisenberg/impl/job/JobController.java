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

import org.joda.time.LocalDateTime;
import org.joda.time.ReadablePeriod;

import com.heisenberg.impl.instance.ProcessInstanceImpl;


/**
 * @author Walter White
 */
public interface JobController {
  
  /** allows process jobs to get the locked process instance */
  ProcessInstanceImpl getProcessInstance();

  void rescheduleFromNow(ReadablePeriod period);
  
  void rescheduleFor(LocalDateTime duedate);
  
  void log(String msg);
}

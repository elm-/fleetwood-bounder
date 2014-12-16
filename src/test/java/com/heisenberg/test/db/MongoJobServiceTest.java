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
package com.heisenberg.test.db;

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.test.other.JobServiceTest;


/**
 * @author Walter White
 */
public class MongoJobServiceTest extends JobServiceTest {

  @Override
  public void initializeProcessEngine() {
    this.processEngine = new MongoProcessEngineConfiguration()
      .registerJobType(TestJob.class)
      .buildProcessEngine();
  }

  
}

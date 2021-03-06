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
package com.heisenberg.test.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.mongo.MongoWorkflowEngine;
import com.heisenberg.mongo.MongoWorkflowEngineConfiguration;
import com.heisenberg.mongo.MongoWorkflowInstanceStore;


/**
 * @author Walter White
 */
public class MeasuringMongoWorkflowEngine extends MongoWorkflowEngine {
  
  public static final Logger log = LoggerFactory.getLogger(MeasuringMongoWorkflowEngine.class);
  
  MeasuringProcessInstances measuringProcessInstances;
  
  public MeasuringMongoWorkflowEngine(MongoWorkflowEngineConfiguration mongoConfiguration) {
    super(mongoConfiguration);
    measuringProcessInstances = new MeasuringProcessInstances((MongoWorkflowInstanceStore) workflowInstanceStore); 
    workflowInstanceStore = measuringProcessInstances; 
    serviceRegistry.registerService(workflowInstanceStore);
  }

  public void logReport(int i, long testStartMillis) {
    measuringProcessInstances.logReport(i, testStartMillis);
  }
}

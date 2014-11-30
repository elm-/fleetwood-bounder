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

import com.heisenberg.api.NotifyActivityInstanceRequest;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration;
import com.heisenberg.impl.engine.mongodb.MongoProcessEngine;
import com.heisenberg.impl.instance.ProcessInstanceImpl;


/**
 * @author Walter White
 */
public class MeasuringMongoProcessEngine extends MongoProcessEngine {
  
  public static final Logger log = LoggerFactory.getLogger(MeasuringMongoProcessEngine.class);
  
  public long insert = 0;
  public long flush = 0;
  public long notify = 0;
  public long flushAndUnlock = 0;
  public long lock = 0;
  
  synchronized void addInsertMillis(long millis) {
    insert += millis;
  }

  synchronized void addFlushMillis(long millis) {
    flush += millis;
  }

  synchronized void addNotifyMillis(long millis) {
    notify += millis;
  }

  synchronized void addFlushAndUnlockMillis(long millis) {
    flushAndUnlock += millis;
  }

  synchronized void addLockMillis(long millis) {
    lock += millis;
  }

  public MeasuringMongoProcessEngine(MongoConfiguration mongoDbConfiguration) {
    super(mongoDbConfiguration);
  }

  @Override
  public void insertProcessInstance(ProcessInstanceImpl processInstance) {
    long start = System.currentTimeMillis();
    super.insertProcessInstance(processInstance);
    addInsertMillis(System.currentTimeMillis()-start);
  }

  @Override
  public void flush(ProcessInstanceImpl processInstance) {
    long start = System.currentTimeMillis();
    super.flush(processInstance);
    addFlushMillis(System.currentTimeMillis()-start);
  }

  @Override
  public ProcessInstance notifyActivityInstance(NotifyActivityInstanceRequest notifyActivityInstanceRequest) {
    long start = System.currentTimeMillis();
    ProcessInstance processInstance = super.notifyActivityInstance(notifyActivityInstanceRequest);
    addNotifyMillis(System.currentTimeMillis()-start);
    return processInstance;
  }

  @Override
  public void flushAndUnlock(ProcessInstanceImpl processInstance) {
    long start = System.currentTimeMillis();
    super.flushAndUnlock(processInstance);
    addFlushAndUnlockMillis(System.currentTimeMillis()-start);
  }

  @Override
  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(Object activityInstanceId) {
    long start = System.currentTimeMillis();
    ProcessInstanceImpl processInstance = super.lockProcessInstanceByActivityInstanceId(activityInstanceId);
    addLockMillis(System.currentTimeMillis()-start);
    return processInstance;
  }
  
  public void logReport(int i, long testStartMillis) {
    logStat(i, "inserts", insert);
    logStat(i, "flushes", flush);
    logStat(i, "notifies", notify);
    logStat(i, "flushAndUnlock", flushAndUnlock);
    logStat(i, "lock", lock);

    log.info("");
    log.info("Total time in db  : "+(((float)(insert+flush+notify+flushAndUnlock+lock))/1000f)+" seconds");
    log.info("Total time in test: "+(((float)(System.currentTimeMillis()-testStartMillis))/1000f)+" seconds");
  }

  protected void logStat(int i, String name, long millis) {
    log.info("");
    log.info(i+" "+name+" in "+millis+" millis");
    log.info(i+" "+name+" in "+((float)millis/1000f)+" seconds");
    log.info((i*1000f/(float)millis)+" "+name+" per second");
  }
}

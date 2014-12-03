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

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.impl.engine.mongodb.MongoProcessEngine;
import com.heisenberg.impl.engine.mongodb.MongoProcessInstances;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;


/**
 * @author Walter White
 */
public class MeasuringProcessInstances extends MongoProcessInstances {

  public long inserts = 0;
  public long insertMillis = 0;
  public long saves = 0;
  public long saveMillis = 0;
  public long updates = 0;
  public long updateMillis = 0;
  public long findAndModifies = 0;
  public long findAndModifyMillis = 0;
  
  synchronized void addInsertMillis(long millis) {
    inserts++;
    insertMillis += millis;
  }

  synchronized void addSaveMillis(long millis) {
    saves++;
    saveMillis += millis;
  }

  synchronized void addUpdateMillis(long millis) {
    updates++;
    updateMillis += millis;
  }

  synchronized void addFindAndModifyMillis(long millis) {
    findAndModifies++;
    findAndModifyMillis += millis;
  }

  public MeasuringProcessInstances(MongoProcessEngine processEngine, DB db, MongoProcessEngineConfiguration mongoConfiguration) {
    super(processEngine, db, mongoConfiguration);
  }

  @Override
  protected WriteResult insert(BasicDBObject dbObject, WriteConcern writeConcern) {
    long start = System.currentTimeMillis();
    WriteResult writeResult = super.insert(dbObject, writeConcern);
    addInsertMillis(System.currentTimeMillis()-start);
    return writeResult;
  }

  @Override
  protected WriteResult save(BasicDBObject dbObject, WriteConcern writeConcern) {
    long start = System.currentTimeMillis();
    WriteResult writeResult = super.save(dbObject, writeConcern);
    addSaveMillis(System.currentTimeMillis()-start);
    return writeResult;
  }

  @Override
  protected WriteResult update(DBObject query, DBObject update, boolean upsert, boolean multi, WriteConcern writeConcern) {
    long start = System.currentTimeMillis();
    WriteResult writeResult = super.update(query, update, upsert, multi, writeConcern);
    addUpdateMillis(System.currentTimeMillis()-start);
    return writeResult;
  }

  @Override
  protected BasicDBObject findAndModify(DBObject query, DBObject update) {
    long start = System.currentTimeMillis();
    BasicDBObject result = super.findAndModify(query, update);
    addFindAndModifyMillis(System.currentTimeMillis()-start);
    return result;
  }

  public void logReport(int nbrOfProcessExecutions, long testStartMillis) {
    log.info("");
    log.info("Total processExecutions  : "+nbrOfProcessExecutions);
    logStat("inserts", inserts, insertMillis);
    logStat("save", saves, saveMillis);
    logStat("udpate", updates, updateMillis);
    logStat("findAndModify", findAndModifies, findAndModifyMillis);

    log.info("");
    log.info("Total time in db  : "+(((float)(insertMillis+saveMillis+updateMillis+findAndModifyMillis))/1000f)+" seconds");
    log.info("Total time in test: "+(((float)(System.currentTimeMillis()-testStartMillis))/1000f)+" seconds");
  }

  protected void logStat(String name, long timesExecuted, long totalMillis) {
    log.info("");
    log.info(timesExecuted+" "+name+" in "+totalMillis+" millis");
    log.info(timesExecuted+" "+name+" in "+((float)totalMillis/1000f)+" seconds");
    log.info((timesExecuted*1000f/(float)totalMillis)+" "+name+" per second");
  }
}

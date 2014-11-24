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

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.util.Id;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessInstanceFieldNames;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.heisenberg.impl.json.Json;
import com.mongodb.BasicDBObject;


/**
 * @author Walter White
 */
public class MongoProcessInstanceWriter extends MongoWriterHelper {
  
  MongoProcessEngine processEngine;
  Json json;
  ProcessInstanceFieldNames fieldNames;
  
  public MongoProcessInstanceWriter(MongoProcessEngine processEngine, ProcessInstanceFieldNames fieldNames) {
    this.processEngine = processEngine;
    this.json = processEngine.json;
    this.fieldNames = fieldNames;
  }

  public BasicDBObject writeProcessInstance(ProcessInstanceImpl process) {
    BasicDBObject dbProcess = new BasicDBObject();
    List<BasicDBObject> dbActivities= new ArrayList<BasicDBObject>();
    List<BasicDBObject> dbVariables = new ArrayList<BasicDBObject>();
    
    putOptId(dbProcess, fieldNames._id, process.id);
    putOptTime(dbProcess, fieldNames.start, process.start);
    putOptTime(dbProcess, fieldNames.end, process.end);
    putOpt(dbProcess, fieldNames.duration, process.duration);
    putOpt(dbProcess, fieldNames.lock, writeLock(process.lock));
    
    collectActivities(process, dbActivities);
    collectVariables(process, dbVariables);

    if (!dbActivities.isEmpty()) {
      dbProcess.put(fieldNames.activityInstances, dbActivities);
    }
    if (!dbVariables.isEmpty()) {
      dbProcess.put(fieldNames.variableInstances, dbVariables);
    }
    
    return dbProcess;
  }

  protected Object writeLock(LockImpl lock) {
    if (lock==null) {
      return null;
    }
    BasicDBObject dbLock = new BasicDBObject();
    putOptTime(dbLock, fieldNames.time, lock.time);
    putOpt(dbLock, fieldNames.owner, lock.owner);
    return dbLock;
  }

  protected void collectActivities(ScopeInstanceImpl scopeInstance, List<BasicDBObject> dbActivityInstances) {
    if (scopeInstance.variableInstances!=null) {
      ScopeInstanceImpl parent = scopeInstance.getParent();
      Id parentId = (parent!=null ? parent.getId() : null);
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        BasicDBObject dbActivity = new BasicDBObject();
        putOpt(dbActivity, fieldNames.activityDefinitionName, activity.activityDefinitionName);
        putOptId(dbActivity, fieldNames.parent, parentId);
        putOptTime(dbActivity, fieldNames.start, activity.start);
        putOptTime(dbActivity, fieldNames.start, activity.start);
        putOpt(dbActivity, fieldNames.duration, activity.duration);
        dbActivityInstances.add(dbActivity);
        collectActivities(activity, dbActivityInstances);
      }
    }
  }
  
  protected void collectVariables(ScopeInstanceImpl scopeInstance, List<BasicDBObject> dbVariableInstances) {
    if (scopeInstance.variableInstances!=null) {
      ScopeInstanceImpl parent = scopeInstance.getParent();
      Id parentId = (parent!=null ? parent.getId() : null);
      for (VariableInstanceImpl variable: scopeInstance.variableInstances) {
        BasicDBObject dbVariable = new BasicDBObject();
        putOpt(dbVariable, fieldNames.variableDefinitionName, variable.variableDefinitionName);
        putOptId(dbVariable, fieldNames.parent, parentId);
        Object jsonValue = variable.dataType.convertInternalToJsonValue(variable.value);
        putOpt(dbVariable, fieldNames.value, jsonValue);
        dbVariableInstances.add(dbVariable);
      }
    }
  }

}

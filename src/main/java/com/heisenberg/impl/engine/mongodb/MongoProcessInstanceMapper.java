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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.impl.definition.ProcessDefinitionImpl;
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
public class MongoProcessInstanceMapper extends MongoMapper {
  
  public static class Fields {
    public String _id = "_id";
    public String processDefinitionId = "pd";
    public String start = "s";
    public String end = "e";
    public String duration = "d";
    public String activityInstances = "a";
    public String variableInstances = "v";
    public String parent = "p";
    public String variableDefinitionId = "vd";
    public String value = "vl";
    public String activityDefinitionId = "ad";
    public String lock = "l";
    public String time = "t";
    public String owner= "w";
    public String updates = "u";
    public String operations = "o";
  }
  
  Fields fields = new Fields();
  MongoProcessEngine processEngine;
  Json json;
  
  public MongoProcessInstanceMapper(MongoProcessEngine processEngine) {
    this.processEngine = processEngine;
    this.json = processEngine.json;
  }

  public BasicDBObject writeProcessInstance(ProcessInstanceImpl process) {
    BasicDBObject dbProcess = new BasicDBObject();
    
    writeObject(dbProcess, fields._id, process.id);
    writeObject(dbProcess, fields.processDefinitionId, process.processDefinition.id);
    writeTimeOpt(dbProcess, fields.start, process.start);
    writeTimeOpt(dbProcess, fields.end, process.end);
    writeLongOpt(dbProcess, fields.duration, process.duration);
    writeObjectOpt(dbProcess, fields.lock, writeLock(process.lock));
    writeActivities(dbProcess, process);
    writeVariables(dbProcess, process);
    
    return dbProcess;
  }
  
  public ProcessInstanceImpl readProcessInstance(BasicDBObject dbProcess) {
    ProcessInstanceImpl process = new ProcessInstanceImpl();
    process.processEngine = processEngine;
    process.processDefinitionId = dbProcess.get(fields.processDefinitionId);
    ProcessDefinitionImpl processDefinition = processEngine.findProcessDefinitionByIdUsingCache(process.processDefinitionId);
    process.processDefinition = processDefinition;
    process.processInstance = process;
    process.scopeDefinition = process.processDefinition;
    process.id = dbProcess.get(fields._id);
    process.start = readTime(dbProcess, fields.start);
    process.end = readTime(dbProcess, fields.end);
    process.duration = readLong(dbProcess, fields.duration);
    process.lock = readLock((BasicDBObject) dbProcess.get(fields.lock));
    
    Map<Object, ActivityInstanceImpl> allActivityInstances = new HashMap<>();
    Map<Object, Object> parentIds = new HashMap<>();
    List<BasicDBObject> dbActivityInstances = readList(dbProcess, fields.activityInstances);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(process, dbActivityInstance);
        allActivityInstances.put(activityInstance.id, activityInstance);
        parentIds.put(activityInstance.id, dbActivityInstance.get(fields.parent));
      }
    }
    
    for (ActivityInstanceImpl activityInstance: allActivityInstances.values()) {
      Object parentId = parentIds.get(activityInstance.id);
      activityInstance.parent = (parentId!=null ? allActivityInstances.get(parentId) : process);
      activityInstance.parent.addActivityInstance(activityInstance);
    }
    
    parentIds = new HashMap<>();
    Map<Object, VariableInstanceImpl> allVariableInstances = new HashMap<>();
    List<BasicDBObject> dbVariableInstances = readList(dbProcess, fields.variableInstances);
    if (dbVariableInstances!=null) {
      for (BasicDBObject dbVariableInstance: dbVariableInstances) {
        VariableInstanceImpl variableInstance = readVariableInstance(process, dbVariableInstance);
        allVariableInstances.put(variableInstance.id, variableInstance);
        parentIds.put(variableInstance.id, dbVariableInstance.get(fields.parent));
      }
    }

    for (VariableInstanceImpl variableInstance: allVariableInstances.values()) {
      Object parentId = parentIds.get(variableInstance.id);
      variableInstance.parent = (parentId!=null ? allActivityInstances.get(parentId) : process);
      variableInstance.parent.addVariableInstance(variableInstance);
    }

    return process;
  }

  protected BasicDBObject writeLock(LockImpl lock) {
    if (lock==null) {
      return null;
    }
    BasicDBObject dbLock = new BasicDBObject();
    writeTimeOpt(dbLock, fields.time, lock.time);
    writeObjectOpt(dbLock, fields.owner, lock.owner);
    return dbLock;
  }
  
  protected LockImpl readLock(BasicDBObject dbLock) {
    if (dbLock==null) {
      return null;
    }
    LockImpl lock = new LockImpl();
    lock.owner = readString(dbLock, fields.owner);
    lock.time = readTime(dbLock, fields.time);
    return lock;
  }

  protected void writeActivities(BasicDBObject dbProcess, ScopeInstanceImpl scopeInstance) {
    if (scopeInstance.activityInstances!=null) {
      ScopeInstanceImpl parent = scopeInstance.getParent();
      Object parentId = (parent!=null ? parent.getId() : null);
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        BasicDBObject dbActivity = new BasicDBObject();
        writeObject(dbActivity, fields._id, activity.id);
        writeObject(dbActivity, fields.activityDefinitionId, activity.activityDefinitionId);
        writeObjectOpt(dbActivity, fields.parent, parentId);
        writeTimeOpt(dbActivity, fields.start, activity.start);
        writeTimeOpt(dbActivity, fields.end, activity.end);
        writeLongOpt(dbActivity, fields.duration, activity.duration);
        writeListElementOpt(dbProcess, fields.activityInstances, dbActivity);
        writeActivities(dbProcess, activity);
      }
    }
  }
  
  protected ActivityInstanceImpl readActivityInstance(ProcessInstanceImpl processInstance, BasicDBObject dbActivityInstance) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.id = dbActivityInstance.get(fields._id);
    activityInstance.start = readTime(dbActivityInstance, fields.start);
    activityInstance.end = readTime(dbActivityInstance, fields.end);
    activityInstance.duration = readLong(dbActivityInstance, fields.duration);
    activityInstance.activityDefinitionId = dbActivityInstance.get(fields.activityDefinitionId);
    activityInstance.processEngine = processEngine;
    activityInstance.processDefinition = processInstance.processDefinition;
    activityInstance.activityDefinition = processInstance.processDefinition.findActivityDefinition(activityInstance.activityDefinitionId);
    activityInstance.scopeDefinition = activityInstance.activityDefinition;
    activityInstance.processInstance = processInstance; 
    return activityInstance;
  }

  protected void writeVariables(BasicDBObject dbProcess, ScopeInstanceImpl scopeInstance) {
    if (scopeInstance.variableInstances!=null) {
      ScopeInstanceImpl parent = scopeInstance.getParent();
      Object parentId = (parent!=null ? parent.getId() : null);
      for (VariableInstanceImpl variable: scopeInstance.variableInstances) {
        BasicDBObject dbVariable = new BasicDBObject();
        writeObject(dbVariable, fields._id, variable.id);
        writeObject(dbVariable, fields.variableDefinitionId, variable.variableDefinitionId);
        writeObjectOpt(dbVariable, fields.parent, parentId);
        Object jsonValue = variable.dataType.convertInternalToJsonValue(variable.value);
        writeObjectOpt(dbVariable, fields.value, jsonValue);
        writeListElementOpt(dbProcess, fields.variableInstances, dbVariable);
      }
    }
    if (scopeInstance.activityInstances!=null) {
      for (ActivityInstanceImpl activity: scopeInstance.activityInstances) {
        writeVariables(dbProcess, activity);
      }
    }
  }

  protected VariableInstanceImpl readVariableInstance(ProcessInstanceImpl processInstance, BasicDBObject dbVariableInstance) {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.processEngine = processEngine;
    variableInstance.processInstance = processInstance;
    variableInstance.variableDefinitionId = dbVariableInstance.get(fields.variableDefinitionId);
    variableInstance.variableDefinition = processInstance.processDefinition.findVariableDefinition(variableInstance.variableDefinitionId);
    variableInstance.dataType = variableInstance.variableDefinition.dataType;
    variableInstance.dataTypeId = variableInstance.dataType.getId();
    variableInstance.value = variableInstance.dataType.convertJsonToInternalValue(dbVariableInstance.get(fields.value));
    return variableInstance;
  }
}

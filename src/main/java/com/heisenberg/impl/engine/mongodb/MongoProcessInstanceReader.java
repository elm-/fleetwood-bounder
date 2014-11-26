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
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessInstanceFieldNames;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.VariableInstanceImpl;
import com.mongodb.BasicDBObject;


/**
 * @author Walter White
 */
public class MongoProcessInstanceReader extends MongoReaderHelper {

  MongoProcessEngine processEngine;
  ProcessInstanceFieldNames fieldNames;

  public MongoProcessInstanceReader(MongoProcessEngine processEngine, ProcessInstanceFieldNames processInstanceFieldNames) {
    this.processEngine = processEngine;
    this.fieldNames = processInstanceFieldNames;
  }
  
  public ProcessInstanceImpl readProcessInstance(BasicDBObject dbProcess) {
    ProcessInstanceImpl process = new ProcessInstanceImpl();
    process.processEngine = processEngine;
    process.processDefinitionId = dbProcess.get(fieldNames.processDefinitionId);
    ProcessDefinitionImpl processDefinition = processEngine.findProcessDefinitionByIdUsingCache(process.processDefinitionId);
    process.processDefinition = processDefinition;
    process.processInstance = process;
    process.scopeDefinition = process.processDefinition;

    process.id = dbProcess.get(fieldNames._id);
    process.start = getTime(dbProcess, fieldNames.start);
    process.end = getTime(dbProcess, fieldNames.end);
    process.duration = getLong(dbProcess, fieldNames.duration);
    process.lock = readLock((BasicDBObject) dbProcess.get(fieldNames.lock));
    
    Map<Object, ActivityInstanceImpl> allActivityInstances = new HashMap<>();
    Map<Object, Object> parentIds = new HashMap<>();
    List<BasicDBObject> dbActivityInstances = getList(dbProcess, fieldNames.activityInstances);
    if (dbActivityInstances!=null) {
      for (BasicDBObject dbActivityInstance: dbActivityInstances) {
        ActivityInstanceImpl activityInstance = readActivityInstance(process, dbActivityInstance);
        allActivityInstances.put(activityInstance.id, activityInstance);
        parentIds.put(activityInstance.id, dbActivityInstance.get(fieldNames.parent));
      }
    }
    
    for (ActivityInstanceImpl activityInstance: allActivityInstances.values()) {
      Object parentId = parentIds.get(activityInstance.id);
      activityInstance.parent = (parentId!=null ? allActivityInstances.get(parentId) : process);
      activityInstance.parent.addActivityInstance(activityInstance);
    }
    
    parentIds = new HashMap<>();
    Map<Object, VariableInstanceImpl> allVariableInstances = new HashMap<>();
    List<BasicDBObject> dbVariableInstances = getList(dbProcess, fieldNames.variableInstances);
    if (dbVariableInstances!=null) {
      for (BasicDBObject dbVariableInstance: dbVariableInstances) {
        VariableInstanceImpl variableInstance = readVariableInstance(process, dbVariableInstance);
        allVariableInstances.put(variableInstance.id, variableInstance);
        parentIds.put(variableInstance.id, dbVariableInstance.get(fieldNames.parent));
      }
    }

    for (VariableInstanceImpl variableInstance: allVariableInstances.values()) {
      Object parentId = parentIds.get(variableInstance.id);
      variableInstance.parent = (parentId!=null ? allActivityInstances.get(parentId) : process);
      variableInstance.parent.addVariableInstance(variableInstance);
    }

    return process;
  }

  private VariableInstanceImpl readVariableInstance(ProcessInstanceImpl processInstance, BasicDBObject dbVariableInstance) {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.processEngine = processEngine;
    variableInstance.processInstance = processInstance;
    variableInstance.variableDefinitionId = dbVariableInstance.get(fieldNames.variableDefinitionId);
    variableInstance.variableDefinition = processInstance.processDefinition.findVariableDefinition(variableInstance.variableDefinitionId);
    variableInstance.dataType = variableInstance.variableDefinition.dataType;
    variableInstance.dataTypeId = variableInstance.dataType.getId();
    variableInstance.value = variableInstance.dataType.convertJsonToInternalValue(dbVariableInstance.get(fieldNames.value));
    return variableInstance;
  }

  protected ActivityInstanceImpl readActivityInstance(ProcessInstanceImpl processInstance, BasicDBObject dbActivityInstance) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    activityInstance.processEngine = processEngine;
    activityInstance.processDefinition = processInstance.processDefinition;
    activityInstance.id = dbActivityInstance.get(fieldNames._id);
    activityInstance.activityDefinitionId = dbActivityInstance.get(fieldNames.activityDefinitionId);
    activityInstance.activityDefinition = processInstance.processDefinition.findActivityDefinition(activityInstance.activityDefinitionId);
    activityInstance.scopeDefinition = activityInstance.activityDefinition;
    activityInstance.processInstance = processInstance; 
    activityInstance.start = getTime(dbActivityInstance, fieldNames.start);
    activityInstance.end = getTime(dbActivityInstance, fieldNames.end);
    activityInstance.duration = getLong(dbActivityInstance, fieldNames.duration);
    return activityInstance;
  }

  protected LockImpl readLock(BasicDBObject dbLock) {
    if (dbLock==null) {
      return null;
    }
    LockImpl lock = new LockImpl();
    lock.owner = getString(dbLock, fieldNames.owner);
    lock.time = getTime(dbLock, fieldNames.time);
    return lock;
  }
}

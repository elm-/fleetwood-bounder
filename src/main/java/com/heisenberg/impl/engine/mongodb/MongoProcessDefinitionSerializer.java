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
import java.util.Stack;

import org.joda.time.LocalDateTime;

import com.heisenberg.api.type.DataType;
import com.heisenberg.api.util.Id;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionVisitor;
import com.heisenberg.impl.definition.TransitionDefinitionImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessDefinitionFieldNames;
import com.heisenberg.impl.json.Json;
import com.mongodb.BasicDBObject;


/**
 * @author Walter White
 */
public class MongoProcessDefinitionSerializer implements ProcessDefinitionVisitor {

  MongoProcessEngine processEngine;
  Json json;
  BasicDBObject dbProcess = null;
  ProcessDefinitionFieldNames fieldNames;
  Stack<BasicDBObject> dbObjectStack = new Stack<>();
  
  public MongoProcessDefinitionSerializer(MongoProcessEngine processEngine, ProcessDefinitionFieldNames fieldNames) {
    this.processEngine = processEngine;
    this.json = processEngine.json;
    this.fieldNames = fieldNames;
  }

  @Override
  public void startProcessDefinition(ProcessDefinitionImpl process) {
    dbProcess = new BasicDBObject();
    dbObjectStack.push(dbProcess);
    putOptId(dbProcess, fieldNames._id, process.id);
    putOpt(dbProcess, fieldNames.name, process.name);
    putOptTime(dbProcess, fieldNames.deployedTime, process.deployedTime);
    putOpt(dbProcess, fieldNames.deployedBy, process.deployedBy);
    putOptId(dbProcess, fieldNames.organizationId, process.organizationId);
    putOptId(dbProcess, fieldNames.processId, process.processId);
    putOpt(dbProcess, fieldNames.version, process.version);
  }

  @Override
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
    dbObjectStack.pop();
  }

  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activity, int index) {
    BasicDBObject dbParentScope = dbObjectStack.peek(); 
    BasicDBObject dbActivity = new BasicDBObject();
    dbObjectStack.push(dbActivity);
    putOpt(dbActivity, fieldNames.name, activity.name);
    if (activity.activityTypeId==null && activity.activityType!=null && activity.activityType.getId()!=null) {
      activity.activityTypeId = activity.activityType.getId();
    }
    if (activity.activityTypeId!=null) {
      putOpt(dbActivity, fieldNames.activityTypeId, activity.activityTypeId);
    } else {
      if (activity.activityType!=null && activity.activityTypeJson==null) {
        activity.activityTypeJson = json.objectToJsonMap(activity.activityType);
      }
      if (activity.activityTypeJson!=null) {
        putOpt(dbActivity, fieldNames.activityType, activity.activityTypeJson);
      }
    }
    addListElementOpt(dbParentScope, fieldNames.activityDefinitions, dbActivity);
  }

  @Override
  public void endActivityDefinition(ActivityDefinitionImpl activityDefinition, int index) {
    dbObjectStack.pop();
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variable, int index) {
    BasicDBObject dbParentScope = dbObjectStack.peek(); 
    BasicDBObject dbVariable = new BasicDBObject();
    putOpt(dbVariable, fieldNames.name, variable.name);
    if (variable.dataTypeId!=null) {
      putOpt(dbVariable, fieldNames.dataTypeId, variable.dataTypeId);
    } else {
      if (variable.dataType!=null && variable.dataTypeJson==null) {
        variable.dataTypeJson = json.objectToJsonMap(variable.dataType);
      }
      if (variable.dataTypeJson!=null) {
        putOpt(dbVariable, fieldNames.dataType, variable.dataTypeJson);
      }
    }
    if (variable.initialValue!=null && variable.initialValueJson==null && variable.dataType!=null) {
      variable.initialValueJson = variable.dataType.convertInternalToJsonValue(variable.initialValue);
    }
    if (variable.initialValueJson!=null) {
      putOpt(dbVariable, fieldNames.initialValue, variable.initialValueJson);
    }
    addListElementOpt(dbParentScope, fieldNames.variableDefinitions, dbVariable);
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transition, int index) {
    BasicDBObject dbParentScope = dbObjectStack.peek(); 
    BasicDBObject dbTransition = new BasicDBObject();
    putOpt(dbTransition, fieldNames.name, transition.name);
    putOpt(dbTransition, fieldNames.from, transition.fromName!=null ? transition.fromName : (transition.from!=null ? transition.from.name : null));
    putOpt(dbTransition, fieldNames.to, transition.toName!=null ? transition.toName : (transition.to!=null ? transition.to.name : null));
    addListElementOpt(dbParentScope, fieldNames.transitionDefinitions, dbTransition);
  }

  @Override
  public void dataType(DataType dataType, int index) {
    // TODO add process definition data type support in mongodb persistence
  }

  public static void putOpt(BasicDBObject o, String fieldName, Object value) {
    if (fieldName!=null && value!=null) {
      o.put(fieldName, value);
    }
  }
  public static void putOptTime(BasicDBObject o, String fieldName, LocalDateTime value) {
    if (fieldName!=null && value!=null) {
      o.put(fieldName, value.toDate());
    }
  }
  public static void putOptId(BasicDBObject o, String fieldName, Id value) {
    if (fieldName!=null && value!=null) {
      o.put(fieldName, value.getInternal());
    }
  }
  @SuppressWarnings("unchecked")
  public void addListElementOpt(BasicDBObject dbParentScope, String fieldName, Object element) {
    if (element!=null) {
      List<Object> list = (List<Object>) dbParentScope.get(fieldName);
      if (list == null) {
        list = new ArrayList<>();
        dbParentScope.put(fieldName, list);
      }
      list.add(element);
    }
  }

}

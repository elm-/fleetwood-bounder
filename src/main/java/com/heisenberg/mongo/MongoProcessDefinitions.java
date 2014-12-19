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
package com.heisenberg.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.heisenberg.api.builder.ProcessDefinitionQuery;
import com.heisenberg.impl.OrderBy;
import com.heisenberg.impl.OrderByDirection;
import com.heisenberg.impl.OrderByElement;
import com.heisenberg.impl.ProcessDefinitionQueryImpl;
import com.heisenberg.impl.WorkflowStore;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ScopeDefinitionImpl;
import com.heisenberg.impl.definition.TransitionDefinitionImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.plugin.ServiceRegistry;
import com.heisenberg.plugin.Validator;
import com.heisenberg.plugin.activities.ActivityType;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


/**
 * @author Walter White
 */
public class MongoProcessDefinitions extends MongoCollection implements WorkflowStore, Validator {
  
  public static final Logger log = MongoProcessEngine.log;
  
  protected ServiceRegistry serviceRegistry;
  protected JsonService jsonService;
  protected MongoProcessEngineConfiguration.ProcessDefinitionFields fields;
  protected WriteConcern writeConcernInsertProcessDefinition;
  
  public MongoProcessDefinitions() {
  }

  public MongoProcessDefinitions(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
    this.jsonService = serviceRegistry.getService(JsonService.class);
  }

  @Override
  public String createProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return new ObjectId().toString();
  }
  
  @Override
  public void insertProcessDefinition(ProcessDefinitionImpl processDefinition) {
    BasicDBObject dbProcessDefinition = writeProcessDefinition(processDefinition);
    insert(dbProcessDefinition, writeConcernInsertProcessDefinition);
  }
  
  @Override
  public List<ProcessDefinitionImpl> loadProcessDefinitions(ProcessDefinitionQueryImpl query) {
    BasicDBObject q = new BasicDBObject();
    if (query.id!=null) {
      q.append(fields._id, new ObjectId(query.id));
    }
    if (query.name!=null) {
      q.append(fields.name, query.name);
    }
    List<ProcessDefinitionImpl> processes = new ArrayList<ProcessDefinitionImpl>();
    DBCursor cursor = find(q);
    if (query.limit!=null) {
      cursor.limit(query.limit);
    }
    if (query.orderBy!=null) {
      cursor.sort(writeOrderBy(query.orderBy));
    }
    while (cursor.hasNext()) {
      BasicDBObject dbProcess = (BasicDBObject) cursor.next();
      ProcessDefinitionImpl processDefinition = readProcessDefinition(dbProcess);
      processes.add(processDefinition);
    }
    return processes;
  }

  

  
  public ProcessDefinitionImpl readProcessDefinition(BasicDBObject dbProcess) {
    ProcessDefinitionImpl process = new ProcessDefinitionImpl();
    process.id = readId(dbProcess, fields._id);
    process.name = readString(dbProcess, fields.name);
    process.deployedTime = readTime(dbProcess, fields.deployedTime);
    process.deployedBy = readId(dbProcess, fields.deployedBy);
    process.organizationId = readId(dbProcess, fields.organizationId);
    process.processId = readId(dbProcess, fields.processId);
    process.version = readLong(dbProcess, fields.version);
    readActivities(process, dbProcess);
    readVariables(process, dbProcess);
    readTransitions(process, dbProcess);
    // process.visit(new ProcessDefinitionValidator(processEngine));
    return process;
  }
  
  public BasicDBObject writeProcessDefinition(ProcessDefinitionImpl process) {
    BasicDBObject dbProcess = new BasicDBObject();
    Stack<BasicDBObject> dbObjectStack = new Stack<>();
    dbObjectStack.push(dbProcess);
    writeId(dbProcess, fields._id, process.id);
    writeString(dbProcess, fields.name, process.name);
    writeTimeOpt(dbProcess, fields.deployedTime, process.deployedTime);
    writeIdOpt(dbProcess, fields.deployedBy, process.deployedBy);
    writeIdOpt(dbProcess, fields.organizationId, process.organizationId);
    writeIdOpt(dbProcess, fields.processId, process.processId);
    writeObjectOpt(dbProcess, fields.version, process.version);
    writeActivities(process, dbObjectStack);
    writeTransitions(process, dbObjectStack);
    writeVariables(process, dbObjectStack);
    return dbProcess;
  }
  
  protected void readActivities(ScopeDefinitionImpl scope, BasicDBObject dbScope) {
    List<BasicDBObject> dbActivities = readList(dbScope, fields.activityDefinitions);
    if (dbActivities!=null) {
      scope.activityDefinitions = new ArrayList<>();
      for (BasicDBObject dbActivity: dbActivities) {
        ActivityDefinitionImpl activity = new ActivityDefinitionImpl();
        activity.id = readString(dbActivity, fields._id);
        Map<String,Object> activityTypeJson = readObjectMap(dbActivity, fields.activityType);
        activity.activityType = jsonService.jsonMapToObject(activityTypeJson, ActivityType.class);
        readActivities(activity, dbActivity);
        readVariables(activity, dbActivity);
        readTransitions(activity, dbActivity);
        scope.activityDefinitions.add(activity);
      }
    }
  }

  protected void writeActivities(ScopeDefinitionImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.activityDefinitions!=null) {
      for (ActivityDefinitionImpl activity: scope.activityDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbActivity = new BasicDBObject();
        dbObjectStack.push(dbActivity);
        writeString(dbActivity, fields._id, activity.id);
        
        Map<String,Object> activityTypeJson = jsonService.objectToJsonMap(activity.activityType);
        writeObjectOpt(dbActivity, fields.activityType, activityTypeJson);
        
        writeListElementOpt(dbParentScope, fields.activityDefinitions, dbActivity);
        writeActivities(activity, dbObjectStack);
        writeTransitions(activity, dbObjectStack);
        writeVariables(activity, dbObjectStack);
        dbObjectStack.pop();
      }
    }
  }

  protected void readTransitions(ScopeDefinitionImpl scope, BasicDBObject dbScope) {
    List<BasicDBObject> dbTransitions = readList(dbScope, fields.transitionDefinitions);
    if (dbTransitions!=null) {
      scope.transitionDefinitions = new ArrayList<>();
      for (BasicDBObject dbTransition: dbTransitions) {
        TransitionDefinitionImpl transition = new TransitionDefinitionImpl();
        transition.fromId = readString(dbTransition, fields.from);
        transition.toId = readString(dbTransition, fields.to);
        scope.transitionDefinitions.add(transition);
      }
    }
  }
  
  protected void writeTransitions(ScopeDefinitionImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.transitionDefinitions!=null) {
      for (TransitionDefinitionImpl transition: scope.transitionDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbTransition = new BasicDBObject();
        writeIdOpt(dbTransition, fields._id, transition.id);
        writeObjectOpt(dbTransition, fields.from, transition.fromId!=null ? transition.fromId : (transition.from!=null ? transition.from.id : null));
        writeObjectOpt(dbTransition, fields.to, transition.toId!=null ? transition.toId : (transition.to!=null ? transition.to.id : null));
        writeListElementOpt(dbParentScope, fields.transitionDefinitions, dbTransition);
      }
    }
  }

  protected void readVariables(ScopeDefinitionImpl scope, BasicDBObject dbScope) {
    List<BasicDBObject> dbVariables = readList(dbScope, fields.variableDefinitions);
    if (dbVariables!=null) {
      scope.variableDefinitions = new ArrayList<>();
      for (BasicDBObject dbVariable: dbVariables) {
        VariableDefinitionImpl variable = new VariableDefinitionImpl();
        variable.id = readString(dbVariable, fields._id);
        
        Map<String,Object> dataTypeJson = readObjectMap(dbVariable, fields.dataType);
        variable.dataType = jsonService.jsonMapToObject(dataTypeJson, DataType.class);

        Object dbInitialValue = dbVariable.get(fields.initialValue);
        variable.initialValue = variable.dataType
                .convertJsonToInternalValue(dbInitialValue);
        
        scope.variableDefinitions.add(variable);
      }
    }
  }

  protected void writeVariables(ScopeDefinitionImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.variableDefinitions!=null) {
      for (VariableDefinitionImpl variable: scope.variableDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbVariable = new BasicDBObject();
        writeString(dbVariable, fields._id, variable.id);
        
        Map<String,Object> dataTypeJson = jsonService.objectToJsonMap(variable.dataType);
        writeObjectOpt(dbVariable, fields.dataType, dataTypeJson);

        if (variable.initialValue!=null) {
          Object jsonValue = variable.dataType
                  .convertInternalToJsonValue(variable.initialValue);
          writeObjectOpt(dbVariable, fields.initialValue, jsonValue);
        }

        writeListElementOpt(dbParentScope, fields.variableDefinitions, dbVariable);
      }
    }
  }
  
  @Override
  public void addError(String message, Object... messageArgs) {
    throw new RuntimeException("Should not happen when reading process definitions from db: "+String.format(message, messageArgs));
  }

  @Override
  public void addWarning(String message, Object... messageArgs) {
    // warnings should be ignored during reading of process definition from db.
  }
  
  public DBObject writeOrderBy(OrderBy orderBy) {
    BasicDBObject dbOrderBy = new BasicDBObject();
    for (OrderByElement element: orderBy.orderByElements) {
      String dbField = getDbField(element.field);
      int dbDirection = (element.direction==OrderByDirection.ASCENDING ? 1 : -1);
      dbOrderBy.append(dbField, dbDirection);
    }
    return dbOrderBy;
  }

  private String getDbField(String field) {
    if (ProcessDefinitionQuery.FIELD_DEPLOY_TIME.equals(field)) {
      return fields.deployedTime;
    }
    throw new RuntimeException("Unknown field "+field);
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }
}

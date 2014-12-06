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
import java.util.Map;
import java.util.Stack;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.MongoProcessEngineConfiguration;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.configuration.ScriptService;
import com.heisenberg.api.configuration.TaskService;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionValidator;
import com.heisenberg.impl.definition.ScopeDefinitionImpl;
import com.heisenberg.impl.definition.TransitionDefinitionImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.plugin.ActivityTypes;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;


/**
 * @author Walter White
 */
public class MongoProcessDefinitions extends MongoCollection implements Validator {
  
  public static final Logger log = LoggerFactory.getLogger(MongoProcessEngine.class);
  
  protected MongoProcessEngine processEngine;
  protected MongoProcessEngineConfiguration.ProcessDefinitionFields fields;
  protected WriteConcern writeConcernInsertProcessDefinition;

  public MongoProcessDefinitions(MongoProcessEngine processEngine, DB db, MongoProcessEngineConfiguration mongoConfiguration) {
    super(db, mongoConfiguration.getProcessDefinitionsCollectionName());
    this.processEngine = processEngine;
    this.fields = mongoConfiguration.getProcessDefinitionFields();
    this.writeConcernInsertProcessDefinition = getWriteConcern(mongoConfiguration.getWriteConcernInsertProcessDefinition());
    this.isPretty = mongoConfiguration.isPretty();
  }
  
  public ProcessDefinitionImpl readProcessDefinition(BasicDBObject dbProcess) {
    ProcessDefinitionImpl process = new ProcessDefinitionImpl();
    process.id = readId(dbProcess, fields._id);
    process.deployedTime = readTime(dbProcess, fields.deployedTime);
    process.deployedBy = readObject(dbProcess, fields.deployedBy);
    process.organizationId = readString(dbProcess, fields.organizationId);
    process.processId = readObject(dbProcess, fields.processId);
    process.version = readLong(dbProcess, fields.version);
    readActivities(process, dbProcess);
    readVariables(process, dbProcess);
    readTransitions(process, dbProcess);
    process.visit(new ProcessDefinitionValidator(processEngine));
    return process;
  }
  
  public BasicDBObject writeProcessDefinition(ProcessDefinitionImpl process) {
    BasicDBObject dbProcess = new BasicDBObject();
    Stack<BasicDBObject> dbObjectStack = new Stack<>();
    dbObjectStack.push(dbProcess);
    writeId(dbProcess, fields._id, process.id);
    writeTimeOpt(dbProcess, fields.deployedTime, process.deployedTime);
    writeObjectOpt(dbProcess, fields.deployedBy, process.deployedBy);
    writeObjectOpt(dbProcess, fields.organizationId, process.organizationId);
    writeObjectOpt(dbProcess, fields.processId, process.processId);
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
        activity.activityType = processEngine.jsonService
                .jsonMapToObject(activityTypeJson, ActivityType.class);
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
        
        Map<String,Object> activityTypeJson = processEngine.jsonService
                .objectToJsonMap(activity.activityType);
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
        variable.id = readId(dbVariable, fields._id);
        
        Map<String,Object> dataTypeJson = readObjectMap(dbVariable, fields.dataType);
        variable.dataType = processEngine.jsonService
                .jsonMapToObject(dataTypeJson, DataType.class);

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
        writeIdOpt(dbVariable, fields._id, variable.id);
        
        Map<String,Object> dataTypeJson = processEngine.jsonService
                .objectToJsonMap(variable.dataType);
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
  
  @Override
  public ScriptService getScriptService() {
    return processEngine.getScriptService();
  }

  @Override
  public JsonService getJsonService() {
    return processEngine.getJsonService();
  }

  @Override
  public TaskService getTaskService() {
    return processEngine.taskService;
  }

  @Override
  public ActivityTypes getActivityTypes() {
    return processEngine.activityTypes;
  }

  public void insertProcessDefinition(ProcessDefinitionImpl processDefinition) {
    BasicDBObject dbProcessDefinition = writeProcessDefinition(processDefinition);
    insert(dbProcessDefinition, writeConcernInsertProcessDefinition);
  }

  public ProcessDefinitionImpl findProcessDefinitionById(String processDefinitionId) {
    DBObject query = BasicDBObjectBuilder.start()
            .add(fields._id, new ObjectId(processDefinitionId))
            .get();
    BasicDBObject dbProcess = findOne(query);
    return readProcessDefinition(dbProcess);
  }
}
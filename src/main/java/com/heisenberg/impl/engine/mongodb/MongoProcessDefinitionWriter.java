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

import java.util.Stack;

import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ScopeDefinitionImpl;
import com.heisenberg.impl.definition.TransitionDefinitionImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessDefinitionFieldNames;
import com.heisenberg.impl.json.Json;
import com.mongodb.BasicDBObject;


/**
 * @author Walter White
 */
public class MongoProcessDefinitionWriter extends MongoWriterHelper {

  MongoProcessEngine processEngine;
  Json json;
  ProcessDefinitionFieldNames fieldNames;
  
  public MongoProcessDefinitionWriter(MongoProcessEngine processEngine, ProcessDefinitionFieldNames fieldNames) {
    this.processEngine = processEngine;
    this.json = processEngine.json;
    this.fieldNames = fieldNames;
  }

  public BasicDBObject writeProcessDefinition(ProcessDefinitionImpl process) {
    BasicDBObject dbProcess = new BasicDBObject();
    Stack<BasicDBObject> dbObjectStack = new Stack<>();
    dbObjectStack.push(dbProcess);
    putOpt(dbProcess, fieldNames._id, process.id);
    putOptTime(dbProcess, fieldNames.deployedTime, process.deployedTime);
    putOpt(dbProcess, fieldNames.deployedBy, process.deployedBy);
    putOpt(dbProcess, fieldNames.organizationId, process.organizationId);
    putOpt(dbProcess, fieldNames.processId, process.processId);
    putOpt(dbProcess, fieldNames.version, process.version);
    writeActivities(process, dbObjectStack);
    writeTransitions(process, dbObjectStack);
    writeVariables(process, dbObjectStack);
    return dbProcess;
  }

  protected void writeActivities(ScopeDefinitionImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.activityDefinitions!=null) {
      for (ActivityDefinitionImpl activity: scope.activityDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbActivity = new BasicDBObject();
        dbObjectStack.push(dbActivity);
        putOpt(dbActivity, fieldNames._id, activity.id);
        putOpt(dbActivity, fieldNames.activityTypeId, activity.activityTypeId);
        putOpt(dbActivity, fieldNames.activityType, activity.activityTypeJson);
        addListElementOpt(dbParentScope, fieldNames.activityDefinitions, dbActivity);
        writeActivities(activity, dbObjectStack);
        writeTransitions(activity, dbObjectStack);
        writeVariables(activity, dbObjectStack);
        dbObjectStack.pop();
      }
    }
  }

  protected void writeVariables(ScopeDefinitionImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.variableDefinitions!=null) {
      for (VariableDefinitionImpl variable: scope.variableDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbVariable = new BasicDBObject();
        putOpt(dbVariable, fieldNames._id, variable.id);
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
    }
  }

  protected void writeTransitions(ScopeDefinitionImpl scope, Stack<BasicDBObject> dbObjectStack) {
    if (scope.transitionDefinitions!=null) {
      for (TransitionDefinitionImpl transition: scope.transitionDefinitions) {
        BasicDBObject dbParentScope = dbObjectStack.peek(); 
        BasicDBObject dbTransition = new BasicDBObject();
        putOpt(dbTransition, fieldNames._id, transition.id);
        putOpt(dbTransition, fieldNames.from, transition.fromId!=null ? transition.fromId : (transition.from!=null ? transition.from.id : null));
        putOpt(dbTransition, fieldNames.to, transition.toId!=null ? transition.toId : (transition.to!=null ? transition.to.id : null));
        addListElementOpt(dbParentScope, fieldNames.transitionDefinitions, dbTransition);
      }
    }
  }
}

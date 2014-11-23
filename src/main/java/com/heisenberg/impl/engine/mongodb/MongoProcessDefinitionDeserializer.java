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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.heisenberg.api.util.OrganizationId;
import com.heisenberg.api.util.ProcessDefinitionId;
import com.heisenberg.api.util.ProcessId;
import com.heisenberg.api.util.UserId;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.definition.ActivityDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.definition.ProcessDefinitionValidator;
import com.heisenberg.impl.definition.ScopeDefinitionImpl;
import com.heisenberg.impl.definition.TransitionDefinitionImpl;
import com.heisenberg.impl.definition.VariableDefinitionImpl;
import com.heisenberg.impl.engine.mongodb.MongoConfiguration.ProcessDefinitionFieldNames;
import com.mongodb.BasicDBObject;


/**
 * @author Walter White
 */
public class MongoProcessDefinitionDeserializer implements Validator {

  MongoProcessEngine processEngine;
  ProcessDefinitionFieldNames fieldNames;
  

  public MongoProcessDefinitionDeserializer(MongoProcessEngine processEngine, ProcessDefinitionFieldNames fieldNames) {
    this.processEngine = processEngine;
    this.fieldNames = fieldNames;
  }

  public ProcessDefinitionImpl readProcessDefinition(BasicDBObject dbProcess) {
    ProcessDefinitionImpl process = new ProcessDefinitionImpl();
    process.id = new ProcessDefinitionId(dbProcess.get(fieldNames._id));
    process.name = getString(dbProcess, fieldNames.name);
    process.deployedTime = getTime(dbProcess, fieldNames.deployedTime);
    Object deployedByInternal = dbProcess.get(fieldNames.deployedBy);
    process.deployedBy = (deployedByInternal!=null ? new UserId(deployedByInternal) : null);
    Object organizationIdInternal = dbProcess.get(fieldNames.organizationId);
    process.organizationId = (organizationIdInternal!=null ? new OrganizationId(organizationIdInternal) : null);
    Object processIdInternal = dbProcess.get(fieldNames.processId);
    process.processId = (processIdInternal!=null ? new ProcessId(processIdInternal) : null);
    process.version = getLong(dbProcess, fieldNames.version);
    
    // TODO readDataTypes(process, dbProcess);
    
    readScopeDefinition(process, dbProcess);
    
    process.visit(new ProcessDefinitionValidator(processEngine));
    
    return process;
  }

  protected void readScopeDefinition(ScopeDefinitionImpl scope, BasicDBObject dbScope) {
    List<BasicDBObject> dbActivities = getList(dbScope, fieldNames.activityDefinitions);
    if (dbActivities!=null) {
      scope.activityDefinitions = new ArrayList<>();
      for (BasicDBObject dbActivity: dbActivities) {
        ActivityDefinitionImpl activity = readActivityDefinition(scope, dbActivity);
        scope.activityDefinitions.add(activity);
      }
    }
    List<BasicDBObject> dbVariables = getList(dbScope, fieldNames.variableDefinitions);
    if (dbVariables!=null) {
      scope.variableDefinitions = new ArrayList<>();
      for (BasicDBObject dbVariable: dbVariables) {
        VariableDefinitionImpl variable = readVariableDefinition(scope, dbVariable);
        scope.variableDefinitions.add(variable);
      }
    }
    List<BasicDBObject> dbTransitions = getList(dbScope, fieldNames.transitionDefinitions);
    if (dbTransitions!=null) {
      scope.transitionDefinitions = new ArrayList<>();
      for (BasicDBObject dbTransition: dbTransitions) {
        TransitionDefinitionImpl transitionDefinition = readTransitionDefinition(scope, dbTransition);
        scope.transitionDefinitions.add(transitionDefinition);
      }
    }
  }

  protected ActivityDefinitionImpl readActivityDefinition(ScopeDefinitionImpl parent, BasicDBObject dbActivity) {
    ActivityDefinitionImpl activity = new ActivityDefinitionImpl();
    activity.name = getString(dbActivity, fieldNames.name);
    activity.activityTypeId = getString(dbActivity, fieldNames.activityTypeId);
    activity.activityTypeJson = getMap(dbActivity, fieldNames.activityType);
    return activity;
  }

  protected VariableDefinitionImpl readVariableDefinition(ScopeDefinitionImpl parent, BasicDBObject dbVariable) {
    VariableDefinitionImpl variable = new VariableDefinitionImpl();
    variable.name = getString(dbVariable, fieldNames.name);
    variable.dataTypeId = getString(dbVariable, fieldNames.dataTypeId);
    variable.dataTypeJson = getMap(dbVariable, fieldNames.dataType);
    variable.initialValueJson = getMap(dbVariable, fieldNames.initialValue);
    return variable;
  }

  protected TransitionDefinitionImpl readTransitionDefinition(ScopeDefinitionImpl parent, BasicDBObject dbTransition) {
    TransitionDefinitionImpl transition = new TransitionDefinitionImpl();
    transition.fromName = getString(dbTransition, fieldNames.from);
    transition.toName = getString(dbTransition, fieldNames.to);
    return transition;
  }

  @SuppressWarnings("unchecked")
  protected List<BasicDBObject> getList(BasicDBObject dbScope, String fieldName) {
    return (List<BasicDBObject>) dbScope.get(fieldName);
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getMap(BasicDBObject dbObject, String fieldName) {
    return (Map<String,Object>)dbObject.get(fieldName);
  }

  protected String getString(BasicDBObject dbObject, String fieldName) {
    return (String) dbObject.get(fieldName);
  }

  protected LocalDateTime getTime(BasicDBObject dbObject, String fieldName) {
    Date date = (Date)dbObject.get(fieldName);
    return (date!=null ? new LocalDateTime(date) : null);
  }

  protected Long getLong(BasicDBObject dbObject, String fieldName) {
    Number number = (Number) dbObject.get(fieldName);
    return (number!=null ? number.longValue() : null);
  }

  @Override
  public void addError(String message, Object... messageArgs) {
    throw new RuntimeException("Should not happen when reading process definitions from db: "+String.format(message, messageArgs));
  }

  @Override
  public void addWarning(String message, Object... messageArgs) {
    // warnings should be ignored during reading of process definition from db.
  }
}

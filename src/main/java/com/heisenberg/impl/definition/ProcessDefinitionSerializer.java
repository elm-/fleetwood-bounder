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
package com.heisenberg.impl.definition;

import com.heisenberg.impl.ProcessEngineImpl;




/** Prepares the process for json serialization by jackson.
 * 
 * This removes activityType's and dataType's and replaces them with id references where applicable.
 * This means that the process object has to be validated again if you want to use it for execution afterwards. 
 * 
 * @author Walter White
 */
public class ProcessDefinitionSerializer implements ProcessDefinitionVisitor {

  @Override
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }

  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activity, int index) {
//    if ( activity.activityType!=null
//         && activity.activityTypeId==null
//         && activity.activityTypeJson==null ) {
//      ProcessEngineImpl processEngine = activity.processEngine;
//      String activityTypeId = processEngine.activityTypes.getActivityTypeId(activity.activityType);
//      if (activityTypeId!=null) {
//        activity.activityTypeId = activityTypeId;
//      } else {
//        try {
//          activity.activityTypeJson = activity.processEngine.jsonService.objectToJsonMap(activity.activityType);
//        } catch (Exception e) {
//          throw new RuntimeException("Couldn't serialize activity type: "+e.getMessage(), e);
//        }
//      }
//    }
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variable, int index) {
//    if ( variable.dataType!=null
//         && variable.dataTypeId==null
//         && variable.dataTypeJson==null ) {
//      ProcessEngineImpl processEngine = variable.processEngine;
//      String dataTypeId = processEngine.dataTypes.getSingletonDataTypeId(variable.dataType);
//      if (dataTypeId!=null) {
//        variable.dataTypeId = dataTypeId;
//      } else {
//        try {
//          variable.dataTypeJson = variable.processEngine.jsonService.objectToJsonMap(variable.dataType);
//        } catch (Exception e) {
//          throw new RuntimeException("Couldn't serialize data type: "+e.getMessage(), e);
//        }
//      }
//    }
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transitionDefinition, int index) {
    if (transitionDefinition.fromId==null && transitionDefinition.from!=null) {
      transitionDefinition.fromId = transitionDefinition.from.id;
    }
    if (transitionDefinition.toId==null && transitionDefinition.to!=null) {
      transitionDefinition.toId = transitionDefinition.to.id;
    }
  }

  @Override
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }

  @Override
  public void endActivityDefinition(ActivityDefinitionImpl activityDefinition, int index) {
  }
}

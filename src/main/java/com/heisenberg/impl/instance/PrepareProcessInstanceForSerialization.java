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
package com.heisenberg.impl.instance;

import com.heisenberg.impl.engine.operation.Operation;
import com.heisenberg.impl.engine.updates.Update;


/**
 * @author Walter White
 */
public class PrepareProcessInstanceForSerialization implements ProcessInstanceVisitor {

  @Override
  public void startProcessInstance(ProcessInstanceImpl processInstance) {
    if (processInstance.processDefinitionId==null 
            && processInstance.processDefinition!=null) {
      processInstance.processDefinitionId = processInstance.processDefinition.id;
    }
  }

  @Override
  public void endProcessInstance(ProcessInstanceImpl processInstance) {
  }

  @Override
  public void lock(LockImpl lock) {
  }

  @Override
  public void update(Update update, int index) {
  }

  @Override
  public void operation(Operation operation, int index) {
    if (operation.activityInstanceId==null && operation.activityInstance!=null) {
      operation.activityInstanceId = operation.activityInstance.id;
    }
  }

  @Override
  public void asyncOperation(Operation operation, int index) {
    operation(operation, index);
  }

  @Override
  public void startActivityInstance(ActivityInstanceImpl activityInstance, int index) {
    if (activityInstance.activityDefinitionName==null
        && activityInstance.activityDefinition!=null) {
      activityInstance.activityDefinitionName = activityInstance.activityDefinition.name;
    }
  }

  @Override
  public void endActivityInstance(ActivityInstanceImpl activityInstance, int index) {
  }

  @Override
  public void variableInstance(VariableInstanceImpl variableInstance, int index) {
    if (variableInstance.dataTypeId==null
        && variableInstance.dataType!=null) {
      variableInstance.dataTypeId = variableInstance.dataType.getId();
    }
    if (variableInstance.variableDefinitionName==null
        && variableInstance.variableDefinition!=null) {
      variableInstance.variableDefinitionName = variableInstance.variableDefinition.name;
    }
  }
}

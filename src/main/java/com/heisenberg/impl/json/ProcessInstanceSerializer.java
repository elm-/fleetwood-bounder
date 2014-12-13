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
package com.heisenberg.impl.json;

import com.heisenberg.impl.engine.operation.Operation;
import com.heisenberg.impl.engine.updates.Update;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.LockImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.impl.instance.ProcessInstanceVisitor;
import com.heisenberg.impl.instance.VariableInstanceImpl;


/** a {@link ProcessInstanceVisitor} that prepares a process instance for json serialization.
 * 
 * @author Walter White
 */
public class ProcessInstanceSerializer implements ProcessInstanceVisitor {

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
    if (activityInstance.activityDefinitionId==null
        && activityInstance.activityDefinition!=null) {
      activityInstance.activityDefinitionId = activityInstance.activityDefinition.id;
    }
  }

  @Override
  public void endActivityInstance(ActivityInstanceImpl activityInstance, int index) {
  }

  @Override
  public void variableInstance(VariableInstanceImpl variableInstance, int index) {
    if (variableInstance.variableDefinitionId==null
        && variableInstance.variableDefinition!=null) {
      variableInstance.variableDefinitionId = variableInstance.variableDefinition.id;
    }
  }
}
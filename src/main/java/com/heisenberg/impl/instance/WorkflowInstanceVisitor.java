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
public interface WorkflowInstanceVisitor {
  
  void startProcessInstance(WorkflowInstanceImpl processInstance);

  void endProcessInstance(WorkflowInstanceImpl processInstance);

  void lock(LockImpl lock);

  void update(Update update, int index);
  
  void operation(Operation operation, int index);
  
  void asyncOperation(Operation operation, int index);

  void startActivityInstance(ActivityInstanceImpl activityInstance, int index);

  void endActivityInstance(ActivityInstanceImpl activityInstance,int index);
  
  void variableInstance(VariableInstanceImpl variableInstance, int index);
}

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
package com.heisenberg.instance;

import java.util.List;
import java.util.Queue;

import com.heisenberg.engine.operation.Operation;
import com.heisenberg.engine.updates.Update;


/**
 * @author Walter White
 */
public class ProcessInstanceVisitor {
  
  public void visitProcessInstance(ProcessInstanceImpl processInstance) {
    startProcessInstance(processInstance);
    visitActivityInstances(processInstance.getActivityInstances());
    visitOperations(processInstance.getOperations());
    visitUpdates(processInstance.getUpdates());
    if (processInstance.getLock()!=null) visitLock(processInstance.getLock());
    endProcessInstance(processInstance);
  }
  
  protected void visitActivityInstances(List<ActivityInstanceImpl> activityInstances) {
    if (activityInstances!=null) {
      for (ActivityInstanceImpl activityInstance: activityInstances) {
        visitActivityInstance(activityInstance);
      }
    }
  }

  protected void visitActivityInstance(ActivityInstanceImpl activityInstance) {
    startActivityInstance(activityInstance);
    visitActivityInstances(activityInstance.getActivityInstances());
    endActivityInstance(activityInstance);
  }
  
  protected void visitUpdates(List<Update> updates) {
    if (updates!=null) {
      for (Update update: updates) {
        visitUpdate(update);
      }
    }
  }

  protected void visitUpdate(Update update) {
  }

  protected void visitOperations(Queue<Operation> operations) {
    if (operations!=null) {
      for (Operation operation: operations) {
        visitOperation(operation);
      }
    }
  }

  protected void visitOperation(Operation operation) {
  }
  
  protected void visitLock(LockImpl lock) {
  }

  protected void startProcessInstance(ProcessInstanceImpl processInstance) {
  }

  protected void endProcessInstance(ProcessInstanceImpl processInstance) {
  }

  protected void startActivityInstance(ActivityInstanceImpl activityInstance) {
  }

  protected void endActivityInstance(ActivityInstanceImpl activityInstance) {
  }
}

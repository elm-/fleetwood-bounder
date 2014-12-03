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
package com.heisenberg.api.activities.bpmn;

import java.util.List;

import com.heisenberg.api.activities.AbstractActivityType;
import com.heisenberg.api.activities.Binding;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.activities.ControllableActivityInstance;
import com.heisenberg.api.configuration.TaskService;
import com.heisenberg.api.task.Task;


/**
 * @author Walter White
 */
public class UserTask extends AbstractActivityType {
  
  @ConfigurationField("Name")
  Binding<String> name;
  
  @ConfigurationField("Candidates")
  List<Binding<String>> candidates;
  
  @Override
  public String getType() {
    return "userTask";
  }

  @Override
  public String getLabel() {
    return "User task";
  }
  
  @Override
  public void start(ControllableActivityInstance activityInstance) {
    TaskService taskService = activityInstance.getTaskService();
    
    String taskName = activityInstance.getValue(name);
    if (taskName==null) {
      taskName = activityInstance.getActivityDefinition().getId().toString();
    }
    List<String> taskCandidateIds = activityInstance.getValueList(candidates);
    String assigneeId = (taskCandidateIds!=null && taskCandidateIds.size()==1 ? taskCandidateIds.get(0) : null);
    
    Task task = taskService.newTask()
      .name(taskName)
      .assigneeId(assigneeId)
      .candidateIds(taskCandidateIds)
      .activityInstance(activityInstance);
    
    taskService.save(task);
  }
  
  public UserTask nameValue(String nameValue) {
    this.name = new Binding<String>().value(nameValue);
    return this;
  }

  public UserTask nameVariableBinding(Object nameVariableDefinitionId) {
    this.name = new Binding<String>().variableDefinitionId(nameVariableDefinitionId);
    return this;
  }

  public UserTask nameExpressionBinding(String nameExpressionText) {
    this.name = new Binding<String>().expression(nameExpressionText);
    return this;
  }
}

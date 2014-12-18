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
package com.heisenberg.test.execution;

import org.junit.Test;

import com.heisenberg.api.activities.bpmn.UserTask;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.impl.plugin.DataTypeService;
import com.heisenberg.impl.util.Lists;
import com.heisenberg.memory.MemoryProcessEngine;
import com.heisenberg.plugin.DataTypes;
import com.heisenberg.test.TestHelper;


/**
 * @author Walter White
 */
public class ForEachTest {
  
  @Test
  public void testTask() throws Exception {
    MemoryProcessEngine processEngine = new MemoryProcessEngine();
    
    DataTypeService dataTypes = processEngine.getDataTypes();
    
    ProcessDefinitionBuilder process = processEngine.newProcessDefinition();
    
    process.newVariable()
      .id("reviewers")
      .dataType(dataTypes.list(DataTypes.TEXT));
    
    process.newActivity()
      .id("Review")
      .forEach("reviewer", DataTypes.TEXT, "reviewers")
      .activityType(new UserTask()
        .candidateVariable("reviewer")
      );
    
    String processDefinitionId = process
      .deploy()
      .checkNoErrorsAndNoWarnings()
      .getProcessDefinitionId();
    
    ProcessInstance processInstance = processEngine.newStart()
      .processDefinitionId(processDefinitionId)
      .variableValue("reviewers", Lists.of("John", "Jack", "Mary"))
      .startProcessInstance();

    // TODO make it so that the parent activity 
    // instance doesn't have a name and doesn't have the empty variable declaration
    TestHelper.assertOpen(processInstance, "Review", "Review", "Review", "Review");
  }
}

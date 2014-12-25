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

import com.heisenberg.api.DataTypes;
import com.heisenberg.api.activitytypes.DefaultTask;
import com.heisenberg.api.activitytypes.ScriptTask;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.test.WorkflowTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Elmar Weber
 */
public class CustomVariableTypeTest extends WorkflowTest {

  private class CustomObject {

    private String name;

    private CustomObject(String name) {
      this.name = name;
    }

    public String hello() {
      return "Hello " + name;
    }
  }

  @Test
  public void testBasicUsage() {
    DataType customDataType = workflowEngine.getDataTypes().javaBean(CustomObject.class);

    WorkflowBuilder w = workflowEngine
      .newWorkflow();

    w.newVariable().id("custom").dataType(customDataType);
    w.newVariable().id("message").dataType(DataTypes.TEXT);

    w.newActivity()
      .activityType(new ScriptTask()
          .variableMapping("custom",
            "custom")
          .variableMapping("message",
            "message")
          .script("message = custom.hello()")
      )
      .id("a");

    String processDefinitionId = w.deploy();

    WorkflowInstance instance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
        // TODO: check why data types
      .variableValue("custom", new CustomObject("world"), customDataType)
      .startWorkflowInstance();

    assertEquals("Hello World", instance.getVariableValue("message"));
  }

  @Test
  public void testUndeclaredVariable() {
    DataType customDataType = workflowEngine.getDataTypes().javaBean(CustomObject.class);

    WorkflowBuilder w = workflowEngine
      .newWorkflow();

    w.newActivity()
      .activityType(new DefaultTask())
      .id("a");

    String processDefinitionId = w.deploy();

    // this should result in an IAE or something
    try {
      WorkflowInstance instance = workflowEngine.newStart()
        .workflowId(processDefinitionId)
        .variableValue("custom", new CustomObject("world"), customDataType)
        .startWorkflowInstance();

      fail("Expected exception");
    } catch (IllegalArgumentException ex) {
      assertTrue(ex.getMessage().contains("custom"));
    }
  }
}

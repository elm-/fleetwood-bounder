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
import com.heisenberg.api.WorkflowEngine;
import com.heisenberg.api.activitytypes.ScriptTask;
import com.heisenberg.api.activitytypes.StartEvent;
import com.heisenberg.api.builder.WorkflowBuilder;
import com.heisenberg.api.definition.Transition;
import com.heisenberg.api.instance.WorkflowInstance;
import com.heisenberg.api.instance.WorkflowInstanceEventListener;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.test.WorkflowTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author Elmar Weber
 */
public class EventListenerTest extends WorkflowTest {
  private class LoggingListener implements WorkflowInstanceEventListener {
    public final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

    private List<String> events = new ArrayList<>();

    public List<String> getEvents() {
      return Collections.unmodifiableList(events);
    }

    @Override
    public void started(ControllableActivityInstance instance) {
      log.debug("started " + instance);
      events.add("s-" + instance.getActivityId());
    }

    @Override
    public void ended(ControllableActivityInstance instance) {
      log.debug("ended " + instance);
      events.add("e-" + instance.getActivityId());
    }

    @Override
    public void transition(ControllableActivityInstance instance, Transition transition) {
      log.debug("transition " + instance);
      assertEquals(instance.getActivityId(), transition.getFrom().getId());
      events.add(transition.getId());
    }
  }

  @Test
  public void testBasicEvents() {
    LoggingListener listener = new LoggingListener();
    workflowEngine.addListener(listener);

    WorkflowBuilder process = workflowEngine.newWorkflow();

    process.newActivity()
      .activityType(StartEvent.INSTANCE)
      .id("s");
    process.newActivity()
      .activityType(new ScriptTask()) // TODO: empty task
      .id("script");
    process.newActivity()
      .activityType(StartEvent.INSTANCE)
      .id("e");

    process.newTransition().id("s_script").from("s").to("script");
    process.newTransition().id("script_e").from("script").to("e");

    String processDefinitionId = process
      .deploy()
      .checkNoErrorsAndNoWarnings()
      .getWorkflowId();

    WorkflowInstance workflowInstance = workflowEngine.newStart()
      .workflowId(processDefinitionId)
      .startWorkflowInstance();

    assertEquals("s-s", listener.getEvents().get(0));
    assertEquals("e-s", listener.getEvents().get(1));
    assertEquals("s_script", listener.getEvents().get(2));
    assertEquals("s-script", listener.getEvents().get(3));
    assertEquals("e-script", listener.getEvents().get(4));
    assertEquals("script_e", listener.getEvents().get(5));
    assertEquals("s-e", listener.getEvents().get(6));
    assertEquals("e-e", listener.getEvents().get(7));
  }
}

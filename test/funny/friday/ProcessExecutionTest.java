package funny.friday;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import funny.friday.ProcessEngine;
import funny.friday.definition.ProcessDefinition;
import funny.friday.definition.ProcessDefinitionId;
import funny.friday.definition.Variable;
import funny.friday.definition.VariableType;
import funny.friday.instance.ActivityInstance;
import funny.friday.instance.ActivityInstanceId;
import funny.friday.instance.ProcessInstance;
import funny.friday.instance.ProcessInstanceId;
import funny.friday.store.memory.MemoryProcessStore;

/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

/**
 * @author Tom Baeyens
 */
public class ProcessExecutionTest {
  
  @Test
  public void testOne() {
    ProcessEngine processEngine = new ProcessEngine();
    processEngine.setProcessStore(new MemoryProcessStore());
    
    ProcessDefinition processDefinition = processEngine.createNewProcessDefinition();
    Go go = new Go();
    processDefinition.addActivityDefinition(go);
    Wait wait = new Wait();
    processDefinition.addActivityDefinition(wait);

    Variable variable = new Variable();
    variable.setName("v");
    variable.setType(VariableType.TEXT);
    processDefinition.addVariable(variable);

    ProcessDefinitionId processDefinitionId = processEngine.saveProcessDefinition(processDefinition);
    
    ProcessInstance newProcessInstance = processEngine.createNewProcessInstance(processDefinitionId);
    newProcessInstance.setVariableByName("v", new Object());
    newProcessInstance.start();
    newProcessInstance.save();
    ProcessInstanceId processInstanceId = newProcessInstance.getId();
    assertNotNull(processInstanceId);
    
    assertEquals(1, go.activityInstances.size());
    assertTrue(go.activityInstances.get(0).isEnded());
    assertEquals(1, wait.activityInstances.size());
    ActivityInstance waitActivityInstance = wait.activityInstances.get(0);
    assertFalse(waitActivityInstance.isEnded());
    
    ActivityInstanceId waitActivityInstanceId = waitActivityInstance.getId();
    ProcessInstance processInstance = processEngine.createProcessInstanceQuery()
      .activityInstanceId(waitActivityInstanceId)
      .lock();
    
    ActivityInstance activityInstance = processInstance.findActivityInstance(waitActivityInstanceId);
    activityInstance.setVariableByName("v", new Object());
    activityInstance.signal();
    
    processInstance.save();
  }
  
  // static process variables
  // transient execution context variables
}

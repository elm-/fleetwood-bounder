/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.store.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import funny.friday.definition.ProcessDefinition;
import funny.friday.definition.ProcessDefinitionId;
import funny.friday.instance.ProcessInstance;
import funny.friday.instance.ProcessInstanceId;
import funny.friday.store.ProcessDefinitionQuery;
import funny.friday.store.ProcessInstanceQuery;
import funny.friday.store.ProcessStore;


/**
 * @author Tom Baeyens
 */
public class MemoryProcessStore implements ProcessStore {
  
  protected Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = Collections.synchronizedMap(new HashMap<ProcessDefinitionId, ProcessDefinition>());
  protected Map<ProcessInstanceId, ProcessInstance> processInstancess = Collections.synchronizedMap(new HashMap<ProcessInstanceId, ProcessInstance>());
  
  public ProcessDefinition createNewProcessDefinition(ProcessDefinitionId id) {
    if (id==null) {
      id = createProcessDefinitionId();
    }
    ProcessDefinition processDefinition = new ProcessDefinition();
    // It is up to the ProcessStore to decide if the id is assigned now, or at the NewProcessInstance.save() 
    processDefinition.setId(id);
    return processDefinition;
  }

  @Override
  public ProcessDefinitionId saveProcessDefinition(ProcessDefinition processDefinition) {
    ProcessDefinitionId processDefinitionId = processDefinition.getId();
    if (processDefinitionId==null) {
      processDefinitionId = new ProcessDefinitionId();
      processDefinition.setId(processDefinitionId);
    }
    return processDefinitionId;
  }

  @Override
  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return new MemoryProcessDefinitionQuery(this);
  }

  @Override
  public ProcessInstance createNewProcessInstance(ProcessDefinition processDefinition) {
    ProcessInstance newProcessInstance = new ProcessInstance(this, processDefinition);
    // It is up to the ProcessStore to decide if the id is assigned now, or at the NewProcessInstance.save() 
    newProcessInstance.setId(new ProcessInstanceId());
    return newProcessInstance;
  }

  @Override
  public ProcessInstanceQuery createProcessInstanceQuery() {
    return new MemoryProcessInstanceQuery(this);
  }

  @Override
  public ProcessInstanceId saveProcessInstance(ProcessInstance processInstance) {
    ProcessInstanceId processInstanceId = processInstance.getId();
    if (processInstanceId==null) {
      processInstanceId = new ProcessInstanceId();
      processInstance.setId(processInstanceId);
    }
    return processInstanceId;
  }
}

/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday;

import funny.friday.definition.ProcessDefinition;
import funny.friday.definition.ProcessDefinitionId;
import funny.friday.instance.ProcessInstance;
import funny.friday.store.ProcessDefinitionQuery;
import funny.friday.store.ProcessInstanceQuery;
import funny.friday.store.ProcessStore;
import funny.friday.util.Exceptions;



/**
 * @author Tom Baeyens
 */
public class ProcessEngine {
  
  ProcessStore processStore;

  public ProcessDefinition createNewProcessDefinition() {
    return createNewProcessDefinition(null);
  }

  public ProcessDefinition createNewProcessDefinition(ProcessDefinitionId id) {
    return processStore.createNewProcessDefinition(id);
  }

  public ProcessDefinitionId saveProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    return processStore.saveProcessDefinition(processDefinition);
  }
  
  public ProcessDefinitionQuery createProcessDefinitionQuery() {
    return processStore.createProcessDefinitionQuery();
  }

  public ProcessInstance createNewProcessInstance(ProcessDefinitionId processDefinitionId) {
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinition processDefinition = createProcessDefinitionQuery()
      .id(processDefinitionId)
      .get();
    return processStore.createNewProcessInstance(processDefinition);
  }

  public ProcessInstanceQuery createProcessInstanceQuery() {
    return processStore.createProcessInstanceQuery();
  }
  
  public ProcessStore getProcessStore() {
    return processStore;
  }
  
  public void setProcessStore(ProcessStore processStore) {
    this.processStore = processStore;
  }
}

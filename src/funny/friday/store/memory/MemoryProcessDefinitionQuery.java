/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.store.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import funny.friday.definition.ProcessDefinition;
import funny.friday.definition.ProcessDefinitionId;
import funny.friday.store.ProcessDefinitionQuery;


/**
 * @author Tom Baeyens
 */
public class MemoryProcessDefinitionQuery implements ProcessDefinitionQuery {
  
  MemoryProcessStore memoryProcessStore;
  
  protected ProcessDefinitionId processDefinitionId;
  
  public MemoryProcessDefinitionQuery(MemoryProcessStore memoryProcessStore) {
    this.memoryProcessStore = memoryProcessStore;
  }

  @Override
  public ProcessDefinitionQuery id(ProcessDefinitionId processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public ProcessDefinition get() {
    Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = memoryProcessStore.processDefinitions;
    if (processDefinitionId!=null) {
      return processDefinitions.get(processDefinitionId);
    }
    if (processDefinitions.isEmpty()) {
      return null;
    }
    for (ProcessDefinition processDefinition: processDefinitions.values()) {
      if (satisfiesCriteria(processDefinition)) {
        return processDefinition;
      }
    }
    return null;
  }

  @Override
  public List<ProcessDefinition> asList() {
    Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = memoryProcessStore.processDefinitions;
    List<ProcessDefinition> result = new ArrayList<>();
    for (ProcessDefinition processDefinition: processDefinitions.values()) {
      if (satisfiesCriteria(processDefinition)) {
        result.add(processDefinition);
      }
    }
    return result;
  }

  boolean satisfiesCriteria(ProcessDefinition processDefinition) {
    if ( processDefinitionId!=null
         && !processDefinitionId.equals(processDefinition.getId()) ) {
      return false;
    }
    return true;
  }
}

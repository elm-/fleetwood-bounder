/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.store;

import java.util.List;

import funny.friday.definition.ProcessDefinition;
import funny.friday.definition.ProcessDefinitionId;


/**
 * @author Tom Baeyens
 */
public interface ProcessDefinitionQuery {

  ProcessDefinitionQuery id(ProcessDefinitionId processDefinitionId);
  
  ProcessDefinition get();

  List<ProcessDefinition> asList();

}

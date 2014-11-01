/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.store;

import funny.friday.definition.ActivityDefinition;
import funny.friday.definition.ActivityDefinitionId;
import funny.friday.definition.ProcessDefinition;
import funny.friday.definition.ProcessDefinitionId;
import funny.friday.instance.ProcessInstance;
import funny.friday.instance.ProcessInstanceId;


/**
 * @author Tom Baeyens
 */
public interface ProcessStore {

  ProcessDefinition createNewProcessDefinition(ProcessDefinitionId id);

  ProcessDefinitionId saveProcessDefinition(ProcessDefinition processDefinition);

  ProcessDefinitionQuery createProcessDefinitionQuery();

  ProcessInstance createNewProcessInstance(ProcessDefinition processDefinition);

  ProcessInstanceQuery createProcessInstanceQuery();

  ProcessInstanceId saveProcessInstance(ProcessInstance processInstance);

  ActivityDefinitionId createActivityDefinitionId(ProcessDefinition processDefinition, ActivityDefinition activityDefinition);

}

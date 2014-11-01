/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.instance;

import funny.friday.definition.ProcessDefinition;
import funny.friday.store.ProcessStore;


/**
 * @author Tom Baeyens
 */
public class ProcessInstance extends CompositeInstance {
  
  protected ProcessInstanceId id;
  
  public ProcessInstance(ProcessStore processStore, ProcessDefinition processDefinition) {
    super(processStore, processDefinition);
  }

  public void save() {
    processStore.saveProcessInstance(this);
  }

  public ProcessInstanceId getId() {
    return id;
  }

  public void setId(ProcessInstanceId id) {
    this.id = id;
  }

  public void start() {
  }
}

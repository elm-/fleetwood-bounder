/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.instance;

import funny.friday.definition.ProcessDefinition;
import funny.friday.store.ProcessStore;


/**
 * @author Tom Baeyens
 */
public class CompositeInstance {

  protected ProcessStore processStore;
  protected ProcessDefinition processDefinition;

  public CompositeInstance(ProcessStore processStore, ProcessDefinition processDefinition) {
    this.processStore = processStore;
    this.processDefinition = processDefinition;
  }

  public ProcessStore getProcessStore() {
    return processStore;
  }

  public void setProcessStore(ProcessStore processStore) {
    this.processStore = processStore;
  }

  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }

  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
  
  /** searches for the variable starting in this activity and upwards over the parent hierarchy */ 
  public void setVariableByName(String variableName, Object value) {
    
  }

  /** scans this activity and the nested activities */
  public ActivityInstance findActivityInstance(ActivityInstanceId activityInstanceId) {
    return null;
  }
}

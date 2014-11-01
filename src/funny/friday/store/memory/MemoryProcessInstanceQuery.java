/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.store.memory;

import funny.friday.instance.ActivityInstanceId;
import funny.friday.instance.ProcessInstance;
import funny.friday.store.ProcessInstanceQuery;


/**
 * @author Tom Baeyens
 */
public class MemoryProcessInstanceQuery implements ProcessInstanceQuery {

  protected MemoryProcessStore memoryProcessStore;
  
  public MemoryProcessInstanceQuery(MemoryProcessStore memoryProcessStore) {
    this.memoryProcessStore = memoryProcessStore;
  }

  @Override
  public ProcessInstanceQuery activityInstanceId(ActivityInstanceId activityInstanceId) {
    return null;
  }

  @Override
  public ProcessInstance lock() {
    return null;
  }

}

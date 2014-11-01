/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.instance;

import funny.friday.definition.ActivityDefinition;
import funny.friday.store.ProcessStore;


/**
 * @author Tom Baeyens
 */
public class ActivityInstance extends CompositeInstance {
  
  protected ActivityInstanceId id;
  protected ActivityDefinition activityDefinition;
  
  public ActivityInstance(ProcessStore processStore, ActivityDefinition activityDefinition) {
    super(processStore, activityDefinition.getProcessDefinition());
    this.activityDefinition = activityDefinition;
  }

  public boolean isEnded() {
    return false;
  }

  public ActivityInstanceId getId() {
    return id;
  }

  public void setId(ActivityInstanceId id) {
    this.id = id;
  }

  public void signal() {
  }

  public void onwards() {
  }
}

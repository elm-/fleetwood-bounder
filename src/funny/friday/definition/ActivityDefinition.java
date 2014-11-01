/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.definition;

import funny.friday.instance.ActivityInstance;


/**
 * @author Tom Baeyens
 */
public abstract class ActivityDefinition extends Composite {

  protected ActivityDefinitionId id;

  public ActivityDefinitionId getId() {
    return id;
  }
  
  public void setId(ActivityDefinitionId id) {
    this.id = id;
  }

  public abstract void execute(ActivityInstance activityInstance); 
}

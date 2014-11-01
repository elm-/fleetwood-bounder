/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday;

import java.util.ArrayList;
import java.util.List;

import funny.friday.definition.ActivityDefinition;
import funny.friday.instance.ActivityInstance;


/**
 * @author Tom Baeyens
 */
public class Go extends ActivityDefinition {
  
  protected List<ActivityInstance> activityInstances = new ArrayList<>();

  @Override
  public void execute(ActivityInstance activityInstance) {
    activityInstances.add(activityInstance);
    activityInstance.onwards();
  }
}

/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.store;

import funny.friday.instance.ActivityInstanceId;
import funny.friday.instance.ProcessInstance;


/**
 * @author Tom Baeyens
 */
public interface ProcessInstanceQuery {

  ProcessInstanceQuery activityInstanceId(ActivityInstanceId activityInstanceId);

  ProcessInstance lock();

}

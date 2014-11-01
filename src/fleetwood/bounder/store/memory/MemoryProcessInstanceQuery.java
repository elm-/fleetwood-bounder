/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.store.memory;

import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.CompositeInstance;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.store.ProcessInstanceQuery;


/**
 * @author Walter White
 */
public class MemoryProcessInstanceQuery implements ProcessInstanceQuery {

  protected MemoryProcessStore memoryProcessStore;
  
  protected ActivityInstanceId activityInstanceId;
  
  public MemoryProcessInstanceQuery(MemoryProcessStore memoryProcessStore) {
    this.memoryProcessStore = memoryProcessStore;
  }

  @Override
  public ProcessInstanceQuery activityInstanceId(ActivityInstanceId activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
    return this;
  }

  @Override
  public ProcessInstance lock() {
    ProcessInstance processInstance = get();
    if (processInstance==null) { // in doubt if we should return null or throw 
      throw new RuntimeException("Couldn't lock process instance");
    }
    memoryProcessStore.lock(processInstance, 100);
    return processInstance;
  }
  
  public ProcessInstance get() {
    for (ProcessInstance processInstance: memoryProcessStore.processInstances.values()) {
      if (satisfiesCriteria(processInstance)) {
        return processInstance;
      }
    }
    return null;
  }
  
  boolean satisfiesCriteria(ProcessInstance processInstance) {
    if (activityInstanceId!=null && !containsCompositeInstance(processInstance, activityInstanceId)) {
      return false;
    }
    return true;
  }

  boolean containsCompositeInstance(CompositeInstance compositeInstance, ActivityInstanceId activityInstanceId) {
    if (compositeInstance.hasActivityInstances()) {
      for (ActivityInstance activityInstance : compositeInstance.getActivityInstances()) {
        if (containsActivityInstance(activityInstance, activityInstanceId)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean containsActivityInstance(ActivityInstance activityInstance, ActivityInstanceId activityInstanceId) {
    if (activityInstanceId.equals(activityInstance.getId())) {
      return true;
    }
    return containsCompositeInstance(activityInstance, activityInstanceId);
  }
}

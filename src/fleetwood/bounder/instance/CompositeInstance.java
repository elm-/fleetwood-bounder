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

package fleetwood.bounder.instance;

import java.util.ArrayList;
import java.util.List;

import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.CompositeDefinition;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.store.ProcessStore;


/**
 * @author Tom Baeyens
 */
public class CompositeInstance {

  protected ProcessStore processStore;
  protected ProcessDefinition processDefinition;
  protected CompositeDefinition compositeDefinition;
  protected ProcessInstance processInstance;
  protected List<ActivityInstance> activityInstances;

  public void start() {
    List<ActivityDefinition> startActivityDefinitions = compositeDefinition.getStartActivityDefinitions();
    if (startActivityDefinitions!=null) {
      for (ActivityDefinition startActivityDefinition: startActivityDefinitions) {
        ActivityInstance activityInstance = createActivityInstance(startActivityDefinition);
        activityInstance.execute();
      }
    }
  }

  public ActivityInstance createActivityInstance(ActivityDefinition activityDefinition) {
    ActivityInstance activityInstance = new ActivityInstance();
    activityInstance.setProcessStore(processStore);
    activityInstance.setCompositeDefinition(activityDefinition);
    activityInstance.setProcessInstance(processInstance);
    activityInstance.setActivityDefinition(activityDefinition);
    if (activityInstances==null) {
      activityInstances = new ArrayList<>();
    }
    activityInstance.setId(processStore.createActivityInstanceId(activityInstance));
    activityInstances.add(activityInstance);
    return activityInstance;
  }
  
  /** searches for the variable starting in this activity and upwards over the parent hierarchy */ 
  public void setVariableByName(String variableName, Object value) {
  }

  /** scans this activity and the nested activities */
  public ActivityInstance findActivityInstance(ActivityInstanceId activityInstanceId) {
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance: activityInstances) {
        ActivityInstance theOne = activityInstance.findActivityInstance(activityInstanceId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
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
  
  public CompositeDefinition getCompositeDefinition() {
    return compositeDefinition;
  }

  public void setCompositeDefinition(CompositeDefinition compositeDefinition) {
    this.compositeDefinition = compositeDefinition;
  }
  
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }
  
  public List<ActivityInstance> getActivityInstances() {
    return activityInstances;
  }
  
  public void setActivityInstances(List<ActivityInstance> activityInstances) {
    this.activityInstances = activityInstances;
  }

  public boolean hasActivityInstances() {
    return activityInstances!=null && !activityInstances.isEmpty();
  }
}

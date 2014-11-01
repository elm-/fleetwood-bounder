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

package fleetwood.bounder.store;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.CompositeDefinition;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.TransitionDefinition;
import fleetwood.bounder.definition.TransitionDefinitionId;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;

/**
 * @author Walter White
 */
public abstract class ProcessStore {
  
  protected ProcessEngine processEngine;
  
  public void saveProcessDefinition(ProcessDefinition processDefinition) {
    identifyProcessDefinition(processDefinition);
    storeProcessDefinition(processDefinition);
  }

  protected void identifyProcessDefinition(ProcessDefinition processDefinition) {
    if (processDefinition.getId()==null) {
      processDefinition.setId(createProcessDefinitionId(processDefinition));
    }
    identifyComposite(processDefinition);
  }

  protected void identifyComposite(CompositeDefinition compositeDefinition) {
    if (compositeDefinition.hasActivityDefinitions()) {
      for (ActivityDefinition activityDefinition: compositeDefinition.getActivityDefinitions()) {
        if (activityDefinition.getId()==null) {
          activityDefinition.setId(createActivityDefinitionId(activityDefinition));
        }
        identifyComposite(activityDefinition);
      }
    }
    if (compositeDefinition.hasTransitionDefinitions()) {
      for (TransitionDefinition transitionDefinition: compositeDefinition.getTransitionDefinitions()) {
        if (transitionDefinition.getId()==null) {
          transitionDefinition.setId(createTransitionDefinitionId(transitionDefinition));
        }
      }
    }
    if (compositeDefinition.hasVariableDefinitions()) {
      for (VariableDefinition variableDefinition: compositeDefinition.getVariableDefinitions()) {
        if (variableDefinition.getId()==null) {
          variableDefinition.setId(createVariableDefinitionId(variableDefinition));
        }
      }
    }
  }

  protected abstract void storeProcessDefinition(ProcessDefinition processDefinition);

  public abstract ProcessDefinitionQuery createProcessDefinitionQuery();

  public abstract ProcessInstanceQuery createProcessInstanceQuery();

  public abstract ProcessDefinitionId createProcessDefinitionId(ProcessDefinition processDefinition);

  public abstract ActivityDefinitionId createActivityDefinitionId(ActivityDefinition activityDefinition);

  public abstract TransitionDefinitionId createTransitionDefinitionId(TransitionDefinition transition);

  public abstract VariableDefinitionId createVariableDefinitionId(VariableDefinition variableDefinition);

  public abstract ProcessInstanceId createProcessInstanceId(ProcessInstance processInstance);
  
  public abstract ActivityInstanceId createActivityInstanceId(ActivityInstance activityInstance);

  public abstract void saveProcessInstance(ProcessInstance processInstance);

  public abstract void flushUpdates(ProcessInstance processInstance);

  public abstract void flushUpdatesAndUnlock(ProcessInstance processInstance);

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }
}

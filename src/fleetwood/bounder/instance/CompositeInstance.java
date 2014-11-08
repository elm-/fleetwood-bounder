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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.ActivityDefinition;
import fleetwood.bounder.definition.CompositeDefinition;
import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.engine.updates.ActivityInstanceCreateUpdate;
import fleetwood.bounder.json.Serializable;
import fleetwood.bounder.json.Serializer;
import fleetwood.bounder.util.Time;


/**
 * @author Walter White
 */
public abstract class CompositeInstance implements Serializable {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public static final String FIELD_START = "start";
  protected Long start;
  
  public static final String FIELD_END = "end";
  protected Long end;

  public static final String FIELD_DURATION = "duration";
  protected Long duration;

  public static final String FIELD_ACTIVITY_INSTANCES = "activityInstances";
  protected List<ActivityInstance> activityInstances;

  public static final String FIELD_VARIABLE_INSTANCES = "variableInstances";
  protected List<VariableInstance<?>> variableInstances;

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinition processDefinition;
  protected CompositeDefinition compositeDefinition;
  protected ProcessInstance processInstance;
  protected CompositeInstance parent;

  public ActivityInstance createActivityInstance(ActivityDefinition activityDefinition) {
    ActivityInstance activityInstance = processEngine.createActivityInstance(activityDefinition);
    activityInstance.setProcessEngine(processEngine);
    activityInstance.setCompositeDefinition(activityDefinition);
    activityInstance.setProcessInstance(processInstance);
    activityInstance.setParent(this);
    activityInstance.setActivityDefinition(activityDefinition);
    activityInstance.setStart(Time.now());
    if (activityInstances==null) {
      activityInstances = new ArrayList<>();
    }
    activityInstances.add(activityInstance);
    initializeVariableInstances();
    log.debug("Created "+activityInstance);
    processInstance.addUpdate(new ActivityInstanceCreateUpdate(activityInstance));
    return activityInstance;
  }
  
  protected void initializeVariableInstances() {
    List<VariableDefinition<?>> variableDefinitions = compositeDefinition.getVariableDefinitions();
    if (variableDefinitions!=null) {
      for (VariableDefinition<?> variableDefinition: variableDefinitions) {
        VariableInstance<?> variableInstance = variableDefinition.createVariableInstance();
        variableInstance.setProcessEngine(processEngine);
        variableInstance.setParent(this);
        variableInstance.setProcessInstance(processInstance);
        if (variableInstances==null) {
          variableInstances = new ArrayList<>();
        }
        variableInstances.add(variableInstance);
      }
    }
  }

  public abstract void end();

  public boolean hasUnfinishedActivityInstances() {
    if (activityInstances==null) {
      return false;
    }
    for (ActivityInstance activityInstance: activityInstances) {
      if (!activityInstance.isEnded()) {
        return true;
      }
    }
    return false;
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
  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
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
  
  public CompositeInstance getParent() {
    return parent;
  }
  
  public void setParent(CompositeInstance parent) {
    this.parent = parent;
  }

  public Long getStart() {
    return start;
  }
  
  public void setStart(Long start) {
    this.start = start;
  }
  
  public Long getEnd() {
    return end;
  }
  
  public abstract void setEnd(Long end); 
  
  public boolean isEnded() {
    return end!=null;
  }

  protected void serializeCompositeInstanceFields(Serializer serializer) {
    serializer.writeTimeField(FIELD_START, start);
    serializer.writeTimeField(FIELD_END, end);
    serializer.writeNumberField(FIELD_DURATION, end);
    serializer.writeObjectArray(FIELD_ACTIVITY_INSTANCES, activityInstances);
    serializer.writeObjectArray(FIELD_VARIABLE_INSTANCES, variableInstances);
  }
}

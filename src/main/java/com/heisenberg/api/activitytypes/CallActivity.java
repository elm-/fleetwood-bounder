/*
 * Copyright 2014 Heisenberg Enterprises Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.heisenberg.api.activitytypes;

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.instance.ActivityInstanceImpl;
import com.heisenberg.impl.instance.ProcessInstanceImpl;
import com.heisenberg.plugin.Validator;
import com.heisenberg.plugin.activities.AbstractActivityType;
import com.heisenberg.plugin.activities.Binding;
import com.heisenberg.plugin.activities.ConfigurationField;
import com.heisenberg.plugin.activities.ControllableActivityInstance;
import com.heisenberg.plugin.activities.Label;


/**
 * @author Walter White
 */
public class CallActivity extends AbstractActivityType {

  @ConfigurationField
  @Label("Subprocess name")
  Binding<String> subProcessNameBinding;

  @ConfigurationField
  @Label("Subprocess id")
  Binding<String> subProcessIdBinding;
  
  /** specifies which variables of this process (keys) have to be copied to 
   * variables in the called process (values). */
  @ConfigurationField
  @Label("Input variable mappings")
  List<CallMapping> inputMappings;
  
  /** specifies which variables of the called process (keys) have to be copied to 
   * variables in this process (values). */
  @ConfigurationField
  @Label("Output variable mappings")
  List<CallMapping> outputMappings;

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    ActivityInstanceImpl activityInstanceImpl = (ActivityInstanceImpl) activityInstance;
    String subProcessId = null;
    if (subProcessIdBinding!=null) {
      subProcessId = activityInstance.getValue(subProcessIdBinding);
    } else if (subProcessNameBinding!=null) {
      String subProcessName = activityInstance.getValue(subProcessIdBinding);
      ProcessDefinitionImpl subprocess = activityInstanceImpl.processEngine.newProcessDefinitionQuery()
        .name(subProcessName)
        .orderByDeployTimeDescending()
        .get();
      if (subprocess!=null) {
        subProcessId = subprocess.id;
      } else {
        throw new RuntimeException("Couldn't find subprocess by name: "+subProcessName);
      }
    }

    StartBuilder start = activityInstanceImpl.newSubprocessStart(subProcessId);
    
    if (inputMappings!=null) {
      for (CallMapping inputMapping: inputMappings) {
        Object value = activityInstance.getValue(inputMapping.sourceBinding);
        start.variableValue(inputMapping.destinationVariableId, value);
      }
    }
    
    ProcessInstance calledProcessInstance = start.startProcessInstance();
    activityInstanceImpl.setCalledProcessInstanceId(calledProcessInstance.getId()); 
  }
  
  public void calledProcessInstanceEnded(ControllableActivityInstance activityInstance, ProcessInstanceImpl calledProcessInstance) {
    if (outputMappings!=null) {
      for (CallMapping outputMapping: outputMappings) {
        Object value = calledProcessInstance.getValue(outputMapping.sourceBinding);
        activityInstance.setVariableValue(outputMapping.destinationVariableId, value);
      }
    }
  }

  public CallActivity subProcessId(String subProcessId) {
    return subProcessId(new Binding<String>().value(subProcessId));
  }

  public CallActivity subProcessIdExpression(String subProcessIdExpression) {
    return subProcessId(new Binding<String>().expression(subProcessIdExpression));
  }

  public CallActivity subProcessIdVariable(String subProcessIdVariableId) {
    return subProcessId(new Binding<String>().variableDefinitionId(subProcessIdVariableId));
  }

  public CallActivity subProcessId(Binding<String> subProcessIdBinding) {
    this.subProcessIdBinding = subProcessIdBinding;
    return this;
  }
  
  public CallActivity inputMapping(String callerVariableId, String calledVariableId) {
    return inputMapping(new Binding<Object>().variableDefinitionId(callerVariableId), calledVariableId);
  }

  public CallActivity inputMapping(Binding<Object> callerBinding, String calledVariableId) {
    CallMapping inputMapping = new CallMapping()
      .sourceBinding(callerBinding)
      .destinationVariableId(calledVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

  public CallActivity outputMapping(String calledVariableId, String callerVariableId) {
    return outputMapping(new Binding<Object>().variableDefinitionId(calledVariableId), callerVariableId);
  }

  public CallActivity outputMapping(Binding<Object> calledBinding, String callerVariableId) {
    CallMapping inputMapping = new CallMapping()
      .sourceBinding(calledBinding)
      .destinationVariableId(callerVariableId);
    if (inputMappings==null) {
      inputMappings = new ArrayList<>();
    }
    inputMappings.add(inputMapping);
    return this;
  }

  @Override
  public void validate(ActivityDefinition activityDefinition, Validator validator) {
    super.validate(activityDefinition, validator);
  }
}

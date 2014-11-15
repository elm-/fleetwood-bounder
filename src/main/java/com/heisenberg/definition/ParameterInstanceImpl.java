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
package com.heisenberg.definition;

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.definition.ParameterBinding;
import com.heisenberg.api.definition.ParameterInstance;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.spi.ActivityParameter;


/**
 * @author Walter White
 */
public class ParameterInstanceImpl {

  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;
  public ScopeDefinitionImpl parent;
  
  public String name;
  public ActivityParameter parameterDefinition;
  public List<ParameterBindingImpl> parameterBindings;
  
  public void addParameterBinding(ParameterBindingImpl parameterBinding) {
    if (parameterBindings==null) {
      parameterBindings = new ArrayList<ParameterBindingImpl>();
    }
    parameterBindings.add(parameterBinding);
  }
  
  public void prepare() {
  }

  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public ActivityParameter getParameterDefinition() {
    return parameterDefinition;
  }
  
  public void setParameterDefinition(ActivityParameter parameterDefinition) {
    this.parameterDefinition = parameterDefinition;
  }
  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  
  public ScopeDefinitionImpl getParent() {
    return parent;
  }

  
  public void setParent(ScopeDefinitionImpl parent) {
    this.parent = parent;
  }

  
  public List<ParameterBindingImpl> getParameterBindings() {
    return parameterBindings;
  }

  
  public void setParameterBindings(List<ParameterBindingImpl> values) {
    this.parameterBindings = values;
  }

  public void parse(ProcessEngineImpl processEngine, DeployProcessDefinitionResponse response, ProcessDefinitionImpl processDefinition,
          ScopeDefinitionImpl parent, ParameterInstance parameterInstance) {
    this.name = parameterInstance.parameterRefName;
    if (this.name==null) {
      response.addError(parameterInstance.location, "Parameter instance does not have a name");
    }
    if (parameterInstance.parameterBindings!=null && !parameterInstance.parameterBindings.isEmpty()) {
      this.parameterBindings = new ArrayList<ParameterBindingImpl>();
      for (ParameterBinding parameterBinding : parameterInstance.parameterBindings) {
        ParameterBindingImpl parameterBindingImpl = new ParameterBindingImpl();
        parameterBindingImpl.parse(processEngine, response, processDefinition, parent, this, parameterBinding);
        parameterBindings.add(parameterBindingImpl);
      }
    }
  }
}

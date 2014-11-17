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
import java.util.Map;

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
  public ActivityParameter activityParameter;
  public List<ParameterBindingImpl> parameterBindings;
  
  public Long buildLine;
  public Long buildColumn;
  
  public ParameterBindingImpl newParameterBinding() {
    ParameterBindingImpl parameterBinding = new ParameterBindingImpl();
    parameterBinding.processEngine = processEngine;
    parameterBinding.processDefinition = processDefinition;
    parameterBinding.parent = this;
    if (parameterBindings==null) {
      parameterBindings = new ArrayList<ParameterBindingImpl>();
    }
    parameterBindings.add(parameterBinding);
    return parameterBinding;
  }

  public void parse(ParseContext parseContext) {
    if (name==null) {
      parseContext.addError(buildLine, buildColumn, "Parameter instance does not have a name");
    } else {
      ActivityDefinitionImpl activityDefinitionmpl = parseContext.getContextObject(ActivityDefinitionImpl.class);
      String activityTypeId = activityDefinitionmpl.activityType.getId();
      Map<String, ActivityParameter> activityParameters = processEngine
              .activityTypeDescriptors
              .get(activityTypeId)
              .activityParameters;
      activityParameter = activityParameters!=null ? activityParameters.get(name) : null;
      if (activityParameter==null) {
        parseContext.addError(buildLine, buildColumn, "Invalid parameter '%s' for activity type '%s': Must be one of %s", name, activityTypeId, activityParameters.keySet());
      } else {
        if (parameterBindings!=null && !parameterBindings.isEmpty()) {
          for (int i=0; i<parameterBindings.size(); i++) {
            ParameterBindingImpl parameterBinding = parameterBindings.get(i);
            parseContext.pushPathElement(parameterBinding, null, i);
            parameterBinding.parse(parseContext);
            parseContext.popPathElement();
          }
        } else {
          if (Boolean.TRUE.equals(activityParameter.required)) {
            parseContext.addError(buildLine, buildColumn, "Parameter %s is not provided", name);
          } else if (Boolean.TRUE.equals(activityParameter.recommended)) {
            parseContext.addWarning(buildLine, buildColumn, "Parameter %s is not provided", name);
          }
        }
      }
    }
  }

  
  public void prepare() {
  }

  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public ActivityParameter getActivityParameter() {
    return activityParameter;
  }
  
  public void setActivityParameter(ActivityParameter parameterDefinition) {
    this.activityParameter = parameterDefinition;
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

}

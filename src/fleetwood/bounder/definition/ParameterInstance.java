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

package fleetwood.bounder.definition;

import java.util.ArrayList;
import java.util.List;

import fleetwood.bounder.instance.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class ParameterInstance<T> {

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinition processDefinition;
  protected CompositeDefinition parent;
  
  protected String name;
  protected ParameterDefinition<T> parameterDefinition;

  protected List<ParameterValue> values;
  
  public void addParameterValue(ParameterValue parameterValue) {
    if (values==null) {
      values = new ArrayList<ParameterValue>();
    }
    values.add(parameterValue);
  }
  
  public void prepare() {
  }

  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public ParameterDefinition<T> getParameterDefinition() {
    return parameterDefinition;
  }
  
  public void setParameterDefinition(ParameterDefinition<T> parameterDefinition) {
    this.parameterDefinition = parameterDefinition;
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

  
  public CompositeDefinition getParent() {
    return parent;
  }

  
  public void setParent(CompositeDefinition parent) {
    this.parent = parent;
  }

  
  public List<ParameterValue> getValues() {
    return values;
  }

  
  public void setValues(List<ParameterValue> values) {
    this.values = values;
  }
}

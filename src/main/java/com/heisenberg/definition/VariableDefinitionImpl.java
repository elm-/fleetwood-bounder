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

import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.VariableInstanceImpl;
import com.heisenberg.spi.Type;
import com.heisenberg.util.Identifyable;


/**
 * @author Walter White
 */
public class VariableDefinitionImpl {

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinitionImpl processDefinition;  
  protected ScopeDefinitionImpl parent;
  protected String name;
  protected Type type;
  protected Object initialValue;
  
  public VariableDefinitionImpl type(Type type) {
    this.type = type;
    return this;
  }
  
  public void prepare() {
  }
  
  public ScopeDefinitionImpl getParent() {
    return parent;
  }

  public void setParent(ScopeDefinitionImpl parent) {
    this.parent = parent;
  }

  public String getName() {
    return name;
  }
  
  public VariableDefinitionImpl setName(String name) {
    this.name = name;
    return this;
  }
  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }
  
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public Type getType() {
    return type;
  }
  
  public void setType(Type type) {
    this.type = type;
  }

  public VariableInstanceImpl createVariableInstance() {
    VariableInstanceImpl variableInstance = new VariableInstanceImpl();
    variableInstance.setType(type);
    variableInstance.setVariableDefinition(this);
    variableInstance.setValue(initialValue);
    return variableInstance;
  }
}

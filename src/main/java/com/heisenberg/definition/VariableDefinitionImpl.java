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

import com.heisenberg.api.definition.VariableBuilder;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.VariableInstanceImpl;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class VariableDefinitionImpl implements VariableBuilder {

  public String name;
  public Type type;
  public Object initialValue;

  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;  
  public ScopeDefinitionImpl parent;

  protected Long buildLine;
  protected Long buildColumn;
  protected String buildTypeRefId;

  public VariableDefinitionImpl name(String name) {
    this.name = name;
    return this;
  }

  public VariableDefinitionImpl line(Long line) {
    this.buildLine = line;
    return this;
  }

  public VariableDefinitionImpl column(Long column) {
    this.buildColumn = column;
    return this;
  }

  public VariableDefinitionImpl type(String typeRefId) {
    this.buildTypeRefId = typeRefId;
    return this;
  }

  public VariableDefinitionImpl type(Class<?> javaClass) {
    this.buildTypeRefId = javaClass.getName();
    return this;
  }

  public VariableDefinitionImpl type(Type type) {
    this.type = type;
    return this;
  }

  public VariableDefinitionImpl initialValue(Object initialValue) {
    this.initialValue = initialValue;
    return this;
  }
  
  public void validate(ParseContext parseContext) {
    if (name==null) {
      parseContext.addError(buildLine, buildColumn, "Variable does not have a name");
    }
    if (buildTypeRefId!=null || type!=null) {
      if (type==null) {
        this.type = processDefinition.findType(buildTypeRefId);
        if (this.type==null) {
          parseContext.addError(buildLine, buildColumn, "Variable '%s' has unknown type '%s'", name, buildTypeRefId);
        }
      }
      if (type!=null) {
        if (initialValue!=null) {
          try {
            this.initialValue = type.convertApiToInternalValue(initialValue);
          } catch (InvalidApiValueException e) {
            parseContext.addError(buildLine, buildColumn, "Invalid initial value %s for variable %s (%s)", initialValue, name, buildTypeRefId);
          }
        }
      }
    } else {
      parseContext.addError(buildLine, buildColumn, "Variable '%s' does not have a type", name);
    }
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

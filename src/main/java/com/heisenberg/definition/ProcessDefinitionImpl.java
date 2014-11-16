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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.type.TypeDescriptor;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class ProcessDefinitionImpl extends ScopeDefinitionImpl {

  public ProcessDefinitionId id;
  public List<Type> types;
  public Map<String,Type> typesMap;

  public ProcessDefinitionImpl(ProcessEngineImpl processEngine, DeployProcessDefinitionResponse response, ProcessDefinition processDefinition) {
    this.processEngine = processEngine;
    this.processDefinition = this;
    this.processEngine = processEngine; // this is to enable the type lookups @see #findType(String) and VariableDefinitionImpl
    if (processDefinition.typeDescriptors!=null) {
      for (TypeDescriptor typeDescriptor: processDefinition.typeDescriptors) {
        Type type = typeDescriptor.createType();
        if (types==null) {
          types = new ArrayList<>();
          typesMap = new HashMap<>();
        }
        types.add(type);
        typesMap.put(type.getId(), type);
      }
    }
    parse(processEngine, response, this, null, processDefinition);
  }

  public void prepare() {
    this.processDefinition = this;
    super.prepare();
  }
  
  public ProcessDefinitionPath getPath() {
    return new ProcessDefinitionPath();
  }

  public ProcessDefinitionId getId() {
    return id;
  }
  
  public void setId(ProcessDefinitionId id) {
    this.id = id;
  }
  
  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }

  public void visit(ProcessDefinitionVisitor visitor) {
    if (visitor==null) {
      return;
    }
    visitor.startProcessDefinition(this);
    super.visit(visitor);
    visitor.endProcessDefinition(this);
  }

  public Type findType(String typeId) {
    Type type = types!=null ? typesMap.get(typeId) : null;
    if (type!=null) {
      return type;
    }
    return processEngine.types.get(typeId);
  }
}

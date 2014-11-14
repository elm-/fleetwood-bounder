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

import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public class ProcessDefinitionImpl extends ScopeDefinitionImpl {

  protected ProcessDefinitionId id;

  public ProcessDefinitionImpl(ProcessEngineImpl processEngine, ProcessDefinition processDefinition) {
    
  }

  public void prepare() {
    this.processDefinition = this;
    super.prepare();
  }
  
  public ProcessDefinitionPathImpl getPath() {
    Exceptions.checkNotNull(id, "Process definition doesn't have an id yet");
    return new ProcessDefinitionPathImpl(id);
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
}

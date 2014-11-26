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
package com.heisenberg.impl;

import java.util.List;

import com.heisenberg.impl.definition.ProcessDefinitionImpl;


/**
 * @author Walter White
 */
public class ProcessDefinitionQuery {

  protected ProcessEngineImpl processEngine;
  protected Object processDefinitionId;
  protected Integer maxResults;
  
  public ProcessDefinitionQuery(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public ProcessDefinitionQuery processDefinitionId(Object id) {
    setProcessDefinitionId(id);
    return this;
  }
  
  public ProcessDefinitionImpl get() {
    setMaxResults(1);
    List<ProcessDefinitionImpl> processDefinitions = asList();
    if (processDefinitions!=null && !processDefinitions.isEmpty()) {
      return processDefinitions.get(0);
    }
    return null;
  }

  public List<ProcessDefinitionImpl> asList() {
    return processEngine.findProcessDefinitions(this);
  }

  public Object getProcessDefinitionId() {
    return processDefinitionId;
  }
  
  public void setProcessDefinitionId(Object processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setMaxResults(Integer max) {
    this.maxResults = max;
  }
  
  public Integer getMaxResults() {
    return maxResults;
  }
  
  public boolean satisfiesCriteria(ProcessDefinitionImpl processDefinition) {
    if ( processDefinitionId!=null
         && !processDefinitionId.equals(processDefinition.getId()) ) {
      return false;
    }
    return true;
  }
}

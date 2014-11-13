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
package com.heisenberg;

import java.util.List;

import com.heisenberg.definition.ProcessDefinition;
import com.heisenberg.definition.ProcessDefinitionId;


/**
 * @author Walter White
 */
public class ProcessDefinitionQueryBuilder {

  protected ProcessEngine processEngine;
  protected ProcessDefinitionQuery processDefinitionQuery;

  public ProcessDefinitionQueryBuilder(ProcessEngine processEngine) {
    super();
    this.processEngine = processEngine;
    this.processDefinitionQuery = new ProcessDefinitionQuery();
  }
  
  public ProcessDefinitionQueryBuilder processDefinitionId(ProcessDefinitionId id) {
    processDefinitionQuery.setProcessDefinitionId(id);
    return this;
  }
  
  public ProcessDefinition get() {
    processDefinitionQuery.setMaxResults(1);
    List<ProcessDefinition> processDefinitions = asList();
    if (processDefinitions!=null && !processDefinitions.isEmpty()) {
      return processDefinitions.get(0);
    }
    return null;
  }

  public List<ProcessDefinition> asList() {
    return processEngine.findProcessDefinitions(processDefinitionQuery);
  }
}

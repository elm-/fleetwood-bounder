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

import com.heisenberg.api.ProcessDefinitionQuery;
import com.heisenberg.impl.definition.ProcessDefinitionImpl;


/**
 * @author Walter White
 */
public class ProcessDefinitionQueryImpl implements ProcessDefinitionQuery {
  
  public enum Representation {
    EXECUTABLE, // returned process definitions should be validated and resolved from the cache
    SUMMARY,    // (future) returned process definitions should only contain the summary fields for a return in a response
    DETAIL      // (future) returned process definitions should contain the full process information for returning in a response
  }

  public ProcessEngineImpl processEngine;
  public String id;
  public String name;
  public Representation representation;
  public Integer limit;
  public OrderBy orderBy;
  // when adding new fields, don't forget to update method onlyIdIsSpecified

  public ProcessDefinitionQueryImpl(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public boolean onlyIdSpecified() {
    return id!=null && name==null;
  }

  public ProcessDefinitionQueryImpl representation(Representation representation) {
    this.representation = representation;
    return this;
  }
  
  public ProcessDefinitionQueryImpl name(String name) {
    this.name = name;
    return this;
  }

  public ProcessDefinitionQueryImpl id(String id) {
    this.id = id;
    return this;
  }

  public ProcessDefinitionQueryImpl limit(int maxResults) {
    this.limit = maxResults;
    return this;
  }
  
  public ProcessDefinitionQueryImpl orderByDeployTimeDescending() {
    orderBy(FIELD_DEPLOY_TIME, OrderByDirection.DESCENDING);
    return this;
  }
  
  public ProcessDefinitionQueryImpl orderByDeployTimeAscending() {
    orderBy(FIELD_DEPLOY_TIME, OrderByDirection.ASCENDING);
    return this;
  }
  
  protected void orderBy(String field, OrderByDirection direction) {
    if (orderBy==null) {
      orderBy = new OrderBy();
    }
    orderBy.add(field, direction);
  }

  public ProcessDefinitionImpl get() {
    limit(1);
    List<ProcessDefinitionImpl> processDefinitions = asList();
    if (processDefinitions!=null && !processDefinitions.isEmpty()) {
      return processDefinitions.get(0);
    }
    return null;
  }

  public List<ProcessDefinitionImpl> asList() {
    return processEngine.findProcessDefinitions(this);
  }
}

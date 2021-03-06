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

import com.heisenberg.api.builder.WorkflowQuery;
import com.heisenberg.impl.definition.WorkflowImpl;


/**
 * @author Walter White
 */
public class WorkflowQueryImpl implements WorkflowQuery {
  
  public enum Representation {
    EXECUTABLE, // returned process definitions should be validated and resolved from the cache
    SUMMARY,    // (future) returned process definitions should only contain the summary fields for a return in a response
    DETAIL      // (future) returned process definitions should contain the full process information for returning in a response
  }

  public WorkflowEngineImpl processEngine;
  public String id;
  public String name;
  public Representation representation;
  public Integer limit;
  public OrderBy orderBy;
  // when adding new fields, don't forget to update method onlyIdIsSpecified

  public WorkflowQueryImpl(WorkflowEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  public boolean onlyIdSpecified() {
    return id!=null && name==null;
  }

  public WorkflowQueryImpl representation(Representation representation) {
    this.representation = representation;
    return this;
  }
  
  public WorkflowQueryImpl name(String name) {
    this.name = name;
    return this;
  }

  public WorkflowQueryImpl id(String id) {
    this.id = id;
    return this;
  }

  public WorkflowQueryImpl limit(int maxResults) {
    this.limit = maxResults;
    return this;
  }
  
  public WorkflowQueryImpl orderByDeployTimeDescending() {
    orderBy(FIELD_DEPLOY_TIME, OrderByDirection.DESCENDING);
    return this;
  }
  
  public WorkflowQueryImpl orderByDeployTimeAscending() {
    orderBy(FIELD_DEPLOY_TIME, OrderByDirection.ASCENDING);
    return this;
  }
  
  protected void orderBy(String field, OrderByDirection direction) {
    if (orderBy==null) {
      orderBy = new OrderBy();
    }
    orderBy.add(field, direction);
  }

  public WorkflowImpl get() {
    limit(1);
    List<WorkflowImpl> processDefinitions = asList();
    if (processDefinitions!=null && !processDefinitions.isEmpty()) {
      return processDefinitions.get(0);
    }
    return null;
  }

  public List<WorkflowImpl> asList() {
    return processEngine.findWorkflows(this);
  }
}

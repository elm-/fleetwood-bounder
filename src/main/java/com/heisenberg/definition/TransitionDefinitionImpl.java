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

import java.util.Map;

import com.heisenberg.api.definition.TransitionBuilder;
import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class TransitionDefinitionImpl implements TransitionBuilder {

  protected String name;
  protected ActivityDefinitionImpl from;
  protected ActivityDefinitionImpl to;

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinitionImpl processDefinition;
  protected ScopeDefinitionImpl parent;
  
  protected String buildFromActivityDefinitionName;
  protected String buildToActivityDefinitionName;
  protected Long buildLine;
  protected Long buildColumn;

  public TransitionDefinitionImpl name(String name) {
    this.name = name;
    return this;
  }

  public TransitionDefinitionImpl line(Long line) {
    this.buildLine = line;
    return this;
  }

  public TransitionDefinitionImpl column(Long column) {
    this.buildColumn = column;
    return this;
  }
  
  /** Fluent builder to set the source of this transition.
   * @param fromActivityDefinitionName the name of the activity definition. */
  public TransitionDefinitionImpl from(String fromActivityDefinitionName) {
    this.buildFromActivityDefinitionName = fromActivityDefinitionName;
    return this;
  }

  public TransitionDefinitionImpl to(String toActivityDefinitionName) {
    this.buildToActivityDefinitionName = toActivityDefinitionName;
    return this;
  }

  public void validate(ParseContext parseContext) {
    ScopeDefinitionImpl scopeDefinitionmpl = parseContext.getContextObject(ScopeDefinitionImpl.class);
    Map<String, ActivityDefinitionImpl> activityDefinitionsMap = scopeDefinitionmpl.activityDefinitionsMap;
    if (buildFromActivityDefinitionName==null) {
      parseContext.addWarning(buildLine, buildColumn, "Transition does not have from (source) specified");
    } else {
      from = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(buildFromActivityDefinitionName) : null);
      if (from==null) {
        parseContext.addError(buildLine, buildColumn, "Transition has an invalid from (source) '%s' : Should be one of "+activityDefinitionsMap.keySet(), buildFromActivityDefinitionName);
      }
    }
    if (buildToActivityDefinitionName==null) {
      parseContext.addWarning(buildLine, buildColumn, "Transition does not have to (destination) specified");
    } else {
      to = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(buildToActivityDefinitionName) : null);
      if (to==null) {
        parseContext.addError(buildLine, buildColumn, "Transition has an invalid to (destination) '%s' : Should be one of "+activityDefinitionsMap.keySet(), buildToActivityDefinitionName);
      }
    }
  }

  public void prepare() {
  }

  public ActivityDefinitionImpl getFrom() {
    return from;
  }
  
  public void setFrom(ActivityDefinitionImpl from) {
    this.from = from;
  }
  
  public ActivityDefinitionImpl getTo() {
    return to;
  }
  
  public void setTo(ActivityDefinitionImpl to) {
    this.to = to;
  }

  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }

  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  
  public ScopeDefinitionImpl getParent() {
    return parent;
  }

  
  public void setParent(ScopeDefinitionImpl parent) {
    this.parent = parent;
  }

  
  public ProcessDefinitionImpl getProcessDefinition() {
    return processDefinition;
  }

  
  public void setProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
  }

}

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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import com.heisenberg.api.Issue;
import com.heisenberg.api.Issue.IssueType;


/**
 * @author Walter White
 */
public class ParseContext {
  
  static final Map<Class<?>, String> typeNames = new HashMap<>();
  static {
    typeNames.put(ActivityDefinitionImpl.class, ".activityDefinitions");
    typeNames.put(VariableDefinitionImpl.class, ".variableDefinitions");
    typeNames.put(TimerDefinitionImpl.class, ".timerDefinitions");
    typeNames.put(ParameterInstanceImpl.class, ".parameterInstances");
  }
  
  public LinkedList<String> path = new LinkedList<>();
  public Stack<Object> contextObjectStack = new Stack<>();
  public List<Issue> issues;
  
  public void pushPathElement(Object element, String name, int index) {
    if (element instanceof ProcessDefinitionImpl) {
      this.path.push("processDefinition");
    } else {
      String type = typeNames.get(element.getClass());
      this.path.push((type!=null ? type : "")+"["+(name!=null ? name : index)+"]");
    }
    this.contextObjectStack.push(element);
  }
  
  public void popPathElement() {
    this.path.pop();
    this.contextObjectStack.pop();
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getContextObject(Class<T> type) {
    for (int i=contextObjectStack.size()-1; i>=0; i--) {
      Object contextObject = contextObjectStack.get(i);
      if (type.isAssignableFrom(contextObject.getClass())) {
        return (T) contextObject;
      }
    }
    throw new RuntimeException("Couldn't find "+type.getName()+" in the context");
  }
  
  public void addError(Long line, Long column, String message, Object... messageArgs) {
    addIssue(IssueType.error, line, column, message, messageArgs, path);
  }

  public void addWarning(Long line, Long column, String message, Object... messageArgs) {
    addIssue(IssueType.warning, line, column, message, messageArgs);
  }

  protected void addIssue(IssueType issueType, Long line, Long column, String message, Object... messageArgs) {
    if (issues==null) {
      issues = new ArrayList<>();
    }
    issues.add(new Issue(issueType, getPathText(), line, column, message, messageArgs));
  }

  protected String getPathText() {
    StringBuilder pathText = new StringBuilder();
    ListIterator<String> iterator = path.listIterator(path.size());
    while (iterator.hasPrevious()) {
      pathText.append(iterator.previous());
    }
    return pathText.toString();
  }
}

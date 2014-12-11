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
package com.heisenberg.impl.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.builder.ParseIssue.IssueType;
import com.heisenberg.api.builder.ParseIssues;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.api.util.ServiceLocator;
import com.heisenberg.api.util.Validator;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.type.DataType;


/** Validates and wires process definition after it's been built by either the builder api or json deserialization.
 * 
 * @author Walter White
 */
public class ProcessDefinitionValidator implements ProcessDefinitionVisitor, Validator {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessDefinitionValidator.class);
  
  static final Map<Class<?>, String> typeNames = new HashMap<>();
  static {
    typeNames.put(ActivityDefinitionImpl.class, ".activityDefinitions");
    typeNames.put(VariableDefinitionImpl.class, ".variableDefinitions");
    typeNames.put(TimerDefinitionImpl.class, ".timerDefinitions");
    typeNames.put(TransitionDefinitionImpl.class, ".transitionDefinitions");
    typeNames.put(DataType.class, ".type");
  }
  
  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;
  public LinkedList<String> path = new LinkedList<>();
  public Stack<Object> contextObjectStack = new Stack<>();
  public ParseIssues parseIssues = new ParseIssues();
  public Set<Object> activityIds = new HashSet<>();
  public Set<Object> variableIds = new HashSet<>();

  public Stack<ValidationContext> contextStack = new Stack<>();
  private class ValidationContext {
    public ValidationContext(Object element, Object name, int index, Long line, Long column) {
      if (element instanceof ProcessDefinitionImpl) {
        this.pathElement = "processDefinition";
      } else {
        String type = typeNames.get(element.getClass());
        this.pathElement = (type!=null ? type : "")+"["+(name!=null ? name+"|" : "")+index+"]";
      }
      this.object = element;
      this.line = line;
      this.column = column;
    }
    String pathElement;
    Object object;
    Long line;
    Long column;
  }

  public ProcessDefinitionValidator(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  @Override
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.processDefinition.processDefinition = processDefinition;
    this.processDefinition.processEngine = processEngine;
    this.processDefinition.activityDefinitionsMap = new HashMap<>();
    this.processDefinition.variableDefinitionsMap = new HashMap<>();
    pushContext(processDefinition, null, 0, processDefinition.line, processDefinition.column);
  }

  @Override
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
    processDefinition.initializeStartActivities(this);
    endScope(processDefinition);
    popContext();
  }

  
  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activity, int index) {
    activity.processEngine = processEngine;
    activity.processDefinition = processDefinition;
    activity.parent = getContextObject(ScopeDefinitionImpl.class);
    pushContext(activity, activity.id, index, activity.line, activity.column);
    if (activity.id==null || "".equals(activity.id)) {
      addError("Activity has no id");
    } else {
      if (!activityIds.contains(activity.id)) {
        activityIds.add(activity.id);
        processDefinition.activityDefinitionsMap.put(activity.id, activity);
      } else {
        addError("Duplicate activity id '%s'. Activity ids have to be unique in the process.", activity.id);
      }
    }
    if (activity.activityType==null) {
      addError("Activity '%s' has no activityType configured", activity.id);
    }
  }

  @Override
  public void endActivityDefinition(ActivityDefinitionImpl activity, int index) {
    endScope(activity);
    popContext();
  }

  protected void endScope(ScopeDefinitionImpl scope) {
    // validation of activity definitions was moved to the end of the scope as 
    // some need to access and validate the number of incoming/outgoing transitions
    List<ActivityDefinitionImpl> activityDefinitions = scope.activityDefinitions;
    if (activityDefinitions!=null && !activityDefinitions.isEmpty()) {
      for (ActivityDefinitionImpl activity: activityDefinitions) {
        if (activity.activityType!=null) {
          activity.activityType.validate(activity, this);
        }
      }
    }
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variable, int index) {
    pushContext(variable, variable.id, index, variable.line, variable.column);
    if (variable.id==null || "".equals(variable.id)) {
      addError("Variable does not have an id");
    } else {
      if (!variableIds.contains(variable.id)) {
        variableIds.add(variable.id);
        processDefinition.variableDefinitionsMap.put(variable.id, variable);
      } else {
        addError("Duplicate variable name %s. Variables have to be unique in the process.", variable.id);
      }
    }
    if (variable.dataType!=null) {
      variable.dataType.validate(this);
    } else {
      addError("No data type configured for variable %s", variable.id);
    }
    popContext();
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transition, int index) {
    pushContext(transition, transition.id, index, transition.line, transition.column);
    if (transition.fromId==null) {
      addWarning("Transition has no 'from' specified");
    } else {
      transition.from = processDefinition.findActivityDefinition(transition.fromId);
      if (transition.from!=null) {
        transition.from.addOutgoingTransition(transition);
      } else {
        ScopeDefinitionImpl scope = getContextObject(ScopeDefinitionImpl.class);
        addError("Transition has an invalid value for 'from' (%s) : %s", transition.fromId, getExistingActivityNamesText(scope));
      }
    }
    if (transition.toId==null) {
      addWarning("Transition has no 'to' specified");
    } else {
      transition.to = processDefinition.findActivityDefinition(transition.toId);
      if (transition.to!=null) {
        transition.to.addIncomingTransition(transition);
      } else {
        ScopeDefinitionImpl scope = getContextObject(ScopeDefinitionImpl.class);
        addError("Transition has an invalid value for 'to' (%s) : %s", transition.toId, getExistingActivityNamesText(scope));
      }
    }
    popContext();
  }

  public void pushContext(Object element, Object name, int index, Long line, Long column) {
    this.contextStack.push(new ValidationContext(element, name, index, line, column));
  }
  
  public void popContext() {
    this.contextStack.pop();
  }
  
  @SuppressWarnings("unchecked")
  public <T> T getContextObject(Class<T> type) {
    for (int i=contextStack.size()-1; i>=0; i--) {
      ValidationContext context = contextStack.get(i);
      if (type.isAssignableFrom(context.object.getClass())) {
        return (T) context.object;
      }
    }
    throw new RuntimeException("Couldn't find "+type.getName()+" in the context");
  }
  
  protected String getPathText() {
    StringBuilder pathText = new StringBuilder();
    for (ValidationContext validationContext: contextStack) {
      pathText.append(validationContext.pathElement);
    }
    return pathText.toString();
  }

  String getExistingActivityNamesText(ScopeDefinitionImpl scope) {
    List<Object> activityIds = new ArrayList<>();
    if (scope.activityDefinitions!=null) {
      for (ActivityDefinitionImpl activity: scope.activityDefinitions) {
        if (activity.id!=null) {
          activityIds.add(activity.id);
        }
      }
    }
    return (!activityIds.isEmpty() ? "Should be one of "+activityIds : "No activities defined in this scope");
  }

  public void addError(String message, Object... messageArgs) {
    ValidationContext currentContext = contextStack.peek();
    parseIssues.addIssue(IssueType.error, getPathText(), currentContext.line, currentContext.column, message, messageArgs);
  }

  public void addWarning(String message, Object... messageArgs) {
    ValidationContext currentContext = contextStack.peek();
    parseIssues.addIssue(IssueType.warning, getPathText(), currentContext.line, currentContext.column, message, messageArgs);
  }
  
  public ParseIssues getIssues() {
    return parseIssues;
  }

  @Override
  public ServiceLocator getServiceLocator() {
    return processEngine;
  }
}

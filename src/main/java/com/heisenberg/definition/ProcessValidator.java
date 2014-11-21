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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import com.heisenberg.api.ParseIssue.IssueType;
import com.heisenberg.api.ParseIssues;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.DataType;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Validator;


/** Validates and wires process definition after it's been built by either the builder api or json deserialization.
 * 
 * @author Walter White
 */
public class ProcessValidator implements ProcessDefinitionVisitor, Validator {
  
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

  public Stack<ValidationContext> contextStack = new Stack<>();
  private class ValidationContext {
    public ValidationContext(Object element, String name, int index, Long line, Long column) {
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

  public ProcessValidator(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  @Override
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    this.processDefinition.processEngine = processEngine;
    pushContext(processDefinition, null, 0, processDefinition.line, processDefinition.column);
    processDefinition.initializeActivityDefinitionsMap();
  }

  @Override
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
    popContext();
  }

  @Override
  public void dataType(DataType dataType, int index) {
    if (processDefinition.typesMap==null) {
      processDefinition.typesMap = new HashMap<>();
    }
    processDefinition.typesMap.put(dataType.getId(), dataType);
  }
  
  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activity, int index) {
    pushContext(activity, activity.name, index, activity.line, activity.column);
    processDefinition.initializeActivityDefinitionsMap();
    if (activity.name==null || "".equals(activity.name)) {
      addError("Activity has no name");
    }
    if (activity.activityType==null) {
      if (activity.activityTypeId!=null) {
        activity.activityType = processEngine.findActivityType(activity.activityTypeId);
      } else if (activity.activityTypeJsonMap!=null) {
        try {
          activity.activityType = processEngine.json.jsonMapToObject(activity.activityTypeJsonMap, ActivityType.class);
        } catch (Exception e) {
          addError("Activity '%s' has invalid value: %s", activity.name, activity.activityTypeJsonMap);
        }
      }
    }
    if (activity.activityType==null) {
      addError("Activity '%s' has no activityType configured", activity.name);
    } else {
      activity.activityType.validate(this);
    }
  }

  @Override
  public void endActivityDefinition(ActivityDefinitionImpl activityDefinition, int index) {
    popContext();
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variable, int index) {
    pushContext(variable, variable.name, index, variable.line, variable.column);
    if (variable.name==null) {
      addError("Variable does not have a name");
    }
    if (variable.dataType==null) {
      if (variable.dataTypeId!=null) {
        variable.dataType = processDefinition.findDataType(variable.dataTypeId);
        if (variable.dataType==null) {
          addError("Variable '%s' has unknown type '%s'", variable.name, variable.dataTypeId);
        }
      } else if (variable.dataTypeJsonMap!=null) {
        try {
          variable.dataType = processEngine.json.jsonMapToObject(variable.dataTypeJsonMap, DataType.class);
        } catch (Exception e) {
          addError("Activity '%s' has invalid data type: %s", variable.name, variable.dataTypeJsonMap);
        }
      } else {
        addError("Variable '%s' does not have a type", variable.name);
      }
    }
    if (variable.dataType!=null) {
      variable.dataType.validate(this);
      if (variable.initialValue!=null) {
        try {
          variable.initialValue = variable.dataType.convertJsonToInternalValue(variable.initialValue);
        } catch (InvalidApiValueException e) {
          addError("Invalid initial value %s for variable %s (%s)", variable.initialValue, variable.name, variable.dataTypeId);
        }
      }
    }
    popContext();
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transition, int index) {
    pushContext(transition, transition.name, index, transition.line, transition.column);
    ScopeDefinitionImpl scopeDefinitionmpl = getContextObject(ScopeDefinitionImpl.class);
    Map<String, ActivityDefinitionImpl> activityDefinitionsMap = scopeDefinitionmpl.activityDefinitionsMap;
    if (transition.fromName==null) {
      addWarning("Transition does not have from (source) specified");
    } else {
      transition.from = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(transition.fromName) : null);
      if (transition.from==null) {
        addError("Transition has an invalid from (source) '%s' : %s", transition.fromName, getExistingActivityNamesText(activityDefinitionsMap));
      }
    }
    if (transition.toName==null) {
      addWarning("Transition does not have to (destination) specified");
    } else {
      transition.to = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(transition.toName) : null);
      if (transition.to==null) {
        addError("Transition has an invalid to (destination) '%s' : %s", transition.toName, getExistingActivityNamesText(activityDefinitionsMap));
      }
    }
    popContext();
  }

  public void pushContext(Object element, String name, int index, Long line, Long column) {
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
    for (int i=contextStack.size()-1; i>=0; i--) {
      pathText.append(contextStack.get(i).pathElement);
    }
    return pathText.toString();
  }

  String getExistingActivityNamesText(Map<String, ActivityDefinitionImpl> activityDefinitionsMap) {
    String activityNames = (activityDefinitionsMap!=null ? "Should be one of "+activityDefinitionsMap.keySet() : "No activities defined in this scope");
    return activityNames;
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
}

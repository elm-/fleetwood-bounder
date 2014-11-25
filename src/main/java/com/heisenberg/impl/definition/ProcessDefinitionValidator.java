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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.heisenberg.api.ParseIssue.IssueType;
import com.heisenberg.api.ParseIssues;
import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.type.DataType;
import com.heisenberg.api.type.InvalidValueException;
import com.heisenberg.api.util.ActivityDefinitionId;
import com.heisenberg.api.util.Validator;
import com.heisenberg.api.util.VariableDefinitionId;
import com.heisenberg.impl.ProcessEngineImpl;


/** Validates and wires process definition after it's been built by either the builder api or json deserialization.
 * 
 * @author Walter White
 */
public class ProcessDefinitionValidator implements ProcessDefinitionVisitor, Validator {
  
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
  public Set<ActivityDefinitionId> activityIds = new HashSet<>();
  public Set<VariableDefinitionId> variableIds = new HashSet<>();

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
    pushContext(processDefinition, null, 0, processDefinition.line, processDefinition.column);
    this.processDefinition.initializeDataTypesMap();
  }

  @Override
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
    processDefinition.initializeStartActivities(this);
    processDefinition.initializeActivityDefinitionsMap();
    popContext();
  }

  @Override
  public void dataType(DataType dataType, int index) {
//    if (processDefinition.dataTypesMap==null) {
//      processDefinition.dataTypesMap = new HashMap<>();
//    }
//    processDefinition.dataTypesMap.put(dataType.getId(), dataType);
  }
  
  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activity, int index) {
    activity.processEngine = processEngine;
    activity.processDefinition = processDefinition;
    activity.parent = getContextObject(ScopeDefinitionImpl.class);
    pushContext(activity, activity.id, index, activity.line, activity.column);
    if (activity.id==null || "".equals(activity.id)) {
      addError("Activity has no name");
    } else {
      if (activity.parent.activityDefinitionsMap==null) {
        activity.parent.activityDefinitionsMap = new LinkedHashMap<>();
      }
      if (!activityIds.contains(activity.id)) {
        activity.parent.activityDefinitionsMap.put(activity.id, activity);
        activityIds.add(activity.id);
      } else {
        addError("Duplicate activity name '%s'. Activity names have to be unique in the process.", activity.id);
      }
    }
    if (activity.activityType==null) {
      if (activity.activityTypeId!=null) {
        activity.activityType = processEngine.findActivityType(activity.activityTypeId);
        if (activity.activityType==null) {
          addError("Activity '%s' has non-existing activity type id '%s'", activity.id);
        }
      } else if (activity.activityTypeJson!=null) {
        try {
          activity.activityType = processEngine.json.jsonMapToObject(activity.activityTypeJson, ActivityType.class);
        } catch (Exception e) {
          addError("Activity '%s' has invalid json value: %s", activity.id, activity.activityTypeJson);
        }
      }
    } else {
      if (activity.activityType.getId()!=null) {
        activity.activityTypeId = activity.activityType.getId();
      } else {
        try {
          activity.activityTypeJson = processEngine.json.objectToJsonMap(activity.activityType);
        } catch (Exception e) {
          addWarning("Activity '%s' couldn't be serialized to json");
        }
      }
    }
    if (activity.activityType==null) {
      addError("Activity '%s' has no activityType configured", activity.id);
    } else if (activity.activityTypeId==null) {
      activity.activityTypeId = activity.activityType.getId();
    }
  }

  @Override
  public void endActivityDefinition(ActivityDefinitionImpl activity, int index) {
    if (activity.activityType!=null) {
      activity.activityType.validate(activity, this);
    }
    popContext();
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variable, int index) {
    ScopeDefinitionImpl scopeDefinition = getContextObject(ScopeDefinitionImpl.class);
    pushContext(variable, variable.id, index, variable.line, variable.column);
    if (variable.id==null) {
      addError("Variable does not have a name");
    }
    if (variable.dataType==null) {
      if (variable.dataTypeId!=null) {
        variable.dataType = processDefinition.findDataType(variable.dataTypeId);
        if (variable.dataType==null) {
          addError("Variable '%s' has unknown type '%s'", variable.id, variable.dataTypeId);
        }
      } else if (variable.dataTypeJson!=null) {
        variable.dataType = processEngine.json.jsonMapToObject(variable.dataTypeJson, DataType.class);
      } else {
        addError("Variable '%s' does not have a type", variable.id);
      }
    } else {
      if (variable.dataType.getId()!=null) {
        variable.dataTypeId = variable.dataType.getId();
      } else {
        try {
          variable.dataTypeJson = processEngine.json.objectToJsonMap(variable.dataType);
        } catch (Exception e) {
          addWarning("Data type '%s' couldn't be serialized to json");
        }
      }
    }
    if (variable.dataType!=null) {
      variable.dataType.validate(this);
      if (variable.initialValueJson!=null) {
        try {
          variable.initialValueJson = variable.dataType.convertJsonToInternalValue(variable.initialValueJson);
        } catch (InvalidValueException e) {
          addError("Invalid initial value %s for variable %s (%s)", variable.initialValueJson, variable.id, variable.dataTypeId);
        }
      }
      if (scopeDefinition.variableDefinitionsMap==null) {
        scopeDefinition.variableDefinitionsMap = new HashMap<>();
      }
      if (!variableIds.contains(variable.id)) {
        scopeDefinition.variableDefinitionsMap.put(variable.id, variable);
        variableIds.add(variable.id);
      } else {
        addError("Duplicate variable name %s. Variables have to be unique in the process.", variable.id);
      }
    }
    popContext();
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transition, int index) {
    pushContext(transition, transition.id, index, transition.line, transition.column);
    ScopeDefinitionImpl scopeDefinitionmpl = getContextObject(ScopeDefinitionImpl.class);
    Map<ActivityDefinitionId, ActivityDefinitionImpl> activityDefinitionsMap = scopeDefinitionmpl.activityDefinitionsMap;
    if (transition.fromId==null) {
      addWarning("Transition does not have from (source) specified");
    } else {
      transition.from = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(transition.fromId) : null);
      if (transition.from!=null) {
        transition.from.addOutgoingTransition(transition);
      } else {
        addError("Transition has an invalid from (source) '%s' : %s", transition.fromId, getExistingActivityNamesText(activityDefinitionsMap));
      }
    }
    if (transition.toId==null) {
      addWarning("Transition does not have to (destination) specified");
    } else {
      transition.to = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(transition.toId) : null);
      if (transition.to==null) {
        addError("Transition has an invalid to (destination) '%s' : %s", transition.toId, getExistingActivityNamesText(activityDefinitionsMap));
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

  String getExistingActivityNamesText(Map<ActivityDefinitionId, ActivityDefinitionImpl> activityDefinitionsMap) {
    return (activityDefinitionsMap!=null ? "Should be one of "+activityDefinitionsMap.keySet() : "No activities defined in this scope");
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

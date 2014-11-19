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
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import com.heisenberg.api.ParseIssue.IssueType;
import com.heisenberg.api.ParseIssues;
import com.heisenberg.impl.ActivityTypeDescriptor;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class ValidateProcessDefinitionAfterDeserialization implements ProcessDefinitionVisitor {
  
  static final Map<Class<?>, String> typeNames = new HashMap<>();
  static {
    typeNames.put(ActivityDefinitionImpl.class, ".activityDefinitions");
    typeNames.put(VariableDefinitionImpl.class, ".variableDefinitions");
    typeNames.put(TimerDefinitionImpl.class, ".timerDefinitions");
    typeNames.put(TransitionDefinitionImpl.class, ".transitionDefinitions");
    typeNames.put(Type.class, ".type");
  }
  
  public ProcessEngineImpl processEngine;
  public ProcessDefinitionImpl processDefinition;
  public LinkedList<String> path = new LinkedList<>();
  public Stack<Object> contextObjectStack = new Stack<>();
  public ParseIssues parseIssues = new ParseIssues();
  
  public ValidateProcessDefinitionAfterDeserialization(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  @Override
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
    this.processDefinition = processDefinition;
    pushPathElement(processDefinition, null, 0);
    processDefinition.initializeActivityDefinitionsMap();
  }

  @Override
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
    popPathElement();
  }

  @Override
  public void type(Type type, int index) {
    if (processDefinition.typesMap==null) {
      processDefinition.typesMap = new HashMap<>();
    }
    processDefinition.typesMap.put(type.getId(), type);
  }
  
  @Override
  public void startActivityDefinition(ActivityDefinitionImpl activity, int index) {
    pushPathElement(activity, activity.name, index);
    processDefinition.initializeActivityDefinitionsMap();
    if (activity.name==null || "".equals(activity.name)) {
      addError(activity.line, activity.column, "Activity has no name");
    }
    Map<String, ActivityTypeDescriptor> descriptors = processEngine.activityTypeDescriptors;
    ActivityTypeDescriptor descriptor = (descriptors!=null ? descriptors.get(activity.activityTypeId) : null);
    if (descriptor!=null) {
      activity.activityType = descriptor.activityType;
    } else {
      addError(activity.line, activity.column,  
              "Activity %s has invalid type %s.  Must be one of %s", 
              activity.name, 
              activity.activityTypeId,
              descriptors.keySet().toString());
    }
  }

  @Override
  public void endActivityDefinition(ActivityDefinitionImpl activityDefinition, int index) {
    popPathElement();
  }

  @Override
  public void variableDefinition(VariableDefinitionImpl variable, int index) {
    pushPathElement(variable, variable.name, index);
    if (variable.name==null) {
      addError(variable.line, variable.column, "Variable does not have a name");
    }
    if (variable.typeId!=null || variable.type!=null) {
      if (variable.type==null) {
        variable.type = processDefinition.findType(variable.typeId);
        if (variable.type==null) {
          addError(variable.line, variable.column, "Variable '%s' has unknown type '%s'", variable.name, variable.typeId);
        }
      }
      if (variable.type!=null) {
        if (variable.initialValue!=null) {
          try {
            variable.initialValue = variable.type.convertApiToInternalValue(variable.initialValue);
          } catch (InvalidApiValueException e) {
            addError(variable.line, variable.column, "Invalid initial value %s for variable %s (%s)", variable.initialValue, variable.name, variable.typeId);
          }
        }
      }
    } else {
      addError(variable.line, variable.column, "Variable '%s' does not have a type", variable.name);
    }
    popPathElement();
  }

  @Override
  public void transitionDefinition(TransitionDefinitionImpl transition, int index) {
    pushPathElement(transition, transition.name, index);
    ScopeDefinitionImpl scopeDefinitionmpl = getContextObject(ScopeDefinitionImpl.class);
    Map<String, ActivityDefinitionImpl> activityDefinitionsMap = scopeDefinitionmpl.activityDefinitionsMap;
    if (transition.fromName==null) {
      addWarning(transition.line, transition.column, "Transition does not have from (source) specified");
    } else {
      transition.from = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(transition.fromName) : null);
      if (transition.from==null) {
        addError(transition.line, transition.column, "Transition has an invalid from (source) '%s' : %s", transition.fromName, getExistingActivityNamesText(activityDefinitionsMap));
      }
    }
    if (transition.toName==null) {
      addWarning(transition.line, transition.column, "Transition does not have to (destination) specified");
    } else {
      transition.to = (activityDefinitionsMap!=null ? activityDefinitionsMap.get(transition.toName) : null);
      if (transition.to==null) {
        addError(transition.line, transition.column, "Transition has an invalid to (destination) '%s' : %s", transition.toName, getExistingActivityNamesText(activityDefinitionsMap));
      }
    }
    popPathElement();
  }

  public void pushPathElement(Object element, String name, int index) {
    if (element instanceof ProcessDefinitionImpl) {
      this.path.push("processDefinition");
    } else {
      String type = typeNames.get(element.getClass());
      this.path.push((type!=null ? type : "")+"["+(name!=null ? name+"|" : "")+index+"]");
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
  
  protected String getPathText() {
    StringBuilder pathText = new StringBuilder();
    ListIterator<String> iterator = path.listIterator(path.size());
    while (iterator.hasPrevious()) {
      pathText.append(iterator.previous());
    }
    return pathText.toString();
  }



  String getExistingActivityNamesText(Map<String, ActivityDefinitionImpl> activityDefinitionsMap) {
    String activityNames = (activityDefinitionsMap!=null ? "Should be one of "+activityDefinitionsMap.keySet() : "No activities defined in this scope");
    return activityNames;
  }

  public void addError(Long line, Long column, String message, Object... messageArgs) {
    parseIssues.addIssue(IssueType.error, getPathText(), line, column, message, messageArgs);
  }

  public void addWarning(Long line, Long column, String message, Object... messageArgs) {
    parseIssues.addIssue(IssueType.warning, getPathText(), line, column, message, messageArgs);
  }
  
  public ParseIssues getIssues() {
    return parseIssues;
  }
}

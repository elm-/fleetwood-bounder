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

import java.util.List;


/**
 * @author Walter White
 */
public class ProcessDefinitionVisitor {

  /** invoked only for process definitions */
  public void startProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }
  
  /** invoked only for process definitions */
  public void endProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }
  
  /** invoked only for process definitions and activity definitions */
  public void startActivityDefinition(ActivityDefinitionImpl activityDefinition) {
  }

  /** invoked only for process definitions and activity definitions */
  public void endActivityDefinition(ActivityDefinitionImpl activityDefinition) {
  }

  /** visit variable definitions */
  public void variableDefinition(VariableDefinitionImpl variableDefinition) {
  }

  /** visit transition definitions */
  public void transitionDefinition(TransitionDefinitionImpl transitionDefinition) {
  }

  /** overwrite if you want to change the order */
  protected void visitCompositeDefinition(ScopeDefinitionImpl scopeDefinition) {
    visitCompositeActivityDefinitions(scopeDefinition);
    visitCompositeTransitionDefinitions(scopeDefinition);
    visitCompositeVariableDefinitions(scopeDefinition);
  }

  protected void visitCompositeActivityDefinitions(ScopeDefinitionImpl scopeDefinition) {
    List<ActivityDefinitionImpl> activityDefinitions = scopeDefinition.activityDefinitions;
    if (activityDefinitions!=null) {
      for (ActivityDefinitionImpl activityDefinition: activityDefinitions) {
        activityDefinition.visit(this);
      }
    }
  }

  protected void visitCompositeVariableDefinitions(ScopeDefinitionImpl scopeDefinition) {
    List<VariableDefinitionImpl> variableDefinitions = scopeDefinition.variableDefinitions;
    if (variableDefinitions!=null) {
      for (VariableDefinitionImpl variableDefinition: variableDefinitions) {
        variableDefinition(variableDefinition);
      }
    }
  }

  protected void visitCompositeTransitionDefinitions(ScopeDefinitionImpl scopeDefinition) {
    List<TransitionDefinitionImpl> transitionDefinitions = scopeDefinition.transitionDefinitions;
    if (transitionDefinitions!=null) {
      for (TransitionDefinitionImpl transitionDefinition: transitionDefinitions) {
        transitionDefinition(transitionDefinition);
      }
    }
  }
}

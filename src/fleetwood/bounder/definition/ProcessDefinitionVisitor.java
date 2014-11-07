/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.definition;

import java.util.List;


/**
 * @author Walter White
 */
public class ProcessDefinitionVisitor {

  /** invoked only for process definitions */
  public void startProcessDefinition(ProcessDefinition processDefinition) {
  }
  
  /** invoked only for process definitions */
  public void endProcessDefinition(ProcessDefinition processDefinition) {
  }
  
  /** invoked only for process definitions and activity definitions */
  public void startActivityDefinition(ActivityDefinition activityDefinition) {
  }

  /** invoked only for process definitions and activity definitions */
  public void endActivityDefinition(ActivityDefinition activityDefinition) {
  }

  /** visit variable definitions */
  public void variableDefinition(VariableDefinition variableDefinition) {
  }

  /** visit transition definitions */
  public void transitionDefinition(TransitionDefinition transitionDefinition) {
  }

  /** overwrite if you want to change the order */
  protected void visitCompositeDefinition(CompositeDefinition compositeDefinition) {
    visitCompositeActivityDefinitions(compositeDefinition);
    visitCompositeTransitionDefinitions(compositeDefinition);
    visitCompositeVariableDefinitions(compositeDefinition);
  }

  protected void visitCompositeActivityDefinitions(CompositeDefinition compositeDefinition) {
    List<ActivityDefinition> activityDefinitions = compositeDefinition.activityDefinitions;
    if (activityDefinitions!=null) {
      for (ActivityDefinition activityDefinition: activityDefinitions) {
        activityDefinition.visit(this);
      }
    }
  }

  protected void visitCompositeVariableDefinitions(CompositeDefinition compositeDefinition) {
    List<VariableDefinition> variableDefinitions = compositeDefinition.variableDefinitions;
    if (variableDefinitions!=null) {
      for (VariableDefinition variableDefinition: variableDefinitions) {
        variableDefinition(variableDefinition);
      }
    }
  }

  protected void visitCompositeTransitionDefinitions(CompositeDefinition compositeDefinition) {
    List<TransitionDefinition> transitionDefinitions = compositeDefinition.transitionDefinitions;
    if (transitionDefinitions!=null) {
      for (TransitionDefinition transitionDefinition: transitionDefinitions) {
        transitionDefinition(transitionDefinition);
      }
    }
  }
}

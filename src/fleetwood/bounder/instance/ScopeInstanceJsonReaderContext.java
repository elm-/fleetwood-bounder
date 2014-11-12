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

package fleetwood.bounder.instance;

import java.util.ArrayList;
import java.util.List;

import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.ScopeDefinition;
import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.definition.VariableDefinitionId;


/**
 * @author Walter White
 */
public class ScopeInstanceJsonReaderContext {
  
  ScopeInstance scopeInstance;
  List<UnresolvedVariableInstance> unresolvedVariableInstances;
  
  public ScopeInstanceJsonReaderContext(ScopeInstance scopeInstance) {
    this.scopeInstance = scopeInstance;
  }

  public void addUnresolvedVariableInstance(
          VariableInstance variableInstance,
          VariableDefinitionId variableDefinitionId,
          Object valueJson) {
    if (unresolvedVariableInstances==null) {
      unresolvedVariableInstances = new ArrayList<ScopeInstanceJsonReaderContext.UnresolvedVariableInstance>();
    }
    unresolvedVariableInstances.add(new UnresolvedVariableInstance(variableInstance, variableDefinitionId, valueJson));
  }

  public class UnresolvedVariableInstance {
    VariableInstance variableInstance;
    VariableDefinitionId variableDefinitionId;
    Object valueJson;
    public UnresolvedVariableInstance(VariableInstance variableInstance, VariableDefinitionId variableDefinitionId, Object valueJson) {
      this.variableInstance = variableInstance;
      this.variableDefinitionId = variableDefinitionId;
      this.valueJson = valueJson;
    }
    public void resolve(ScopeInstance scopeInstance) {
      ScopeDefinition scopeDefinition = scopeInstance.scopeDefinition;
      VariableDefinition variableDefinition = scopeDefinition.getVariableDefinition(variableDefinitionId);
      variableInstance.setVariableDefinition(variableDefinition);
    }
  }

  public void addUnresolvedActivityInstance(ActivityInstance activityInstance, ActivityDefinitionId activityDefinitionId) {
  }

  public void resolve() {
    if (unresolvedVariableInstances!=null) {
      for (UnresolvedVariableInstance unresolvedVariableInstance: unresolvedVariableInstances) {
        unresolvedVariableInstance.resolve(scopeInstance);
      }
    }
  }
}

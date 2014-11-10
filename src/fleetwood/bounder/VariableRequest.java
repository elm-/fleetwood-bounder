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

package fleetwood.bounder;

import java.util.Map;

import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.VariableInstance;


/**
 * @author Walter White
 */
public class VariableRequest {

  protected Map<VariableDefinitionId,VariableInstance<?>> variablesInstances;
  
  /** extra user defined information to be stored with the process instance. */
  protected Map<String,VariableInstance<?>> persistentContext;
  
  public VariableRequest variableValue(VariableDefinitionId variableDefinitionId, Object value) {
    // TODO
    return this;
  }
  
  public Map<VariableDefinitionId, VariableInstance<?>> getVariablesInstances() {
    return variablesInstances;
  }
  
  public void setVariablesInstances(Map<VariableDefinitionId, VariableInstance<?>> variables) {
    this.variablesInstances = variables;
  }
  
  public Map<String, VariableInstance<?>> getPersistentContext() {
    return persistentContext;
  }

  public void setPersistentContext(Map<String, VariableInstance<?>> persistentContext) {
    this.persistentContext = persistentContext;
  }
}

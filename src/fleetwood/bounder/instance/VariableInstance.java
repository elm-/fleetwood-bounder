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

import fleetwood.bounder.definition.VariableDefinition;
import fleetwood.bounder.json.JsonSerializable;
import fleetwood.bounder.json.JsonSerializer;
import fleetwood.bounder.type.Type;


/**
 * @author Walter White
 */
public class VariableInstance<T> implements JsonSerializable {

  protected ProcessEngineImpl processEngine;
  protected CompositeInstance parent;
  protected ProcessInstance processInstance;
  
  public static final String FIELD_VARIABLE_DEFINITION_ID = "variableDefinitionId";
  protected VariableDefinition<T> variableDefinition;
  protected Type<T> type;
  
  public static final String FIELD_VALUE = "value";
  protected T value;
  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public CompositeInstance getParent() {
    return parent;
  }
  
  public void setParent(CompositeInstance parent) {
    this.parent = parent;
  }
  
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  public Type<T> getType() {
    return type;
  }

  public void setType(Type<T> type) {
    this.type = type;
  }
  
  public T getValue() {
    return value;
  }
  
  public void setValue(T value) {
    this.value = value;
  }
  
  public VariableDefinition<T> getVariableDefinition() {
    return variableDefinition;
  }
  
  public void setVariableDefinition(VariableDefinition<T> variableDefinition) {
    this.variableDefinition = variableDefinition;
  }

  @Override
  public void serialize(JsonSerializer serializer) {
    serializer.objectStart(this);
    serializer.writeIdField(FIELD_VARIABLE_DEFINITION_ID, variableDefinition.getId());
    if (value!=null) {
      type.serializeValueField(serializer, FIELD_VARIABLE_DEFINITION_ID, value);
    }
    serializer.objectEnd(this);
  }
}

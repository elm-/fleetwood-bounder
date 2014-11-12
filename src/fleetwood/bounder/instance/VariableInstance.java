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
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.json.JsonReader;
import fleetwood.bounder.json.JsonWriter;
import fleetwood.bounder.json.Jsonnable;
import fleetwood.bounder.type.Type;


/**
 * @author Walter White
 */
public class VariableInstance implements Jsonnable {

  protected ProcessEngineImpl processEngine;
  protected ScopeInstance parent;
  protected ProcessInstance processInstance;
  
  public static final String FIELD_VARIABLE_DEFINITION_ID = "variableDefinitionId";
  protected VariableDefinition variableDefinition;
  protected Type type;
  
  public static final String FIELD_VALUE = "value";
  protected Object value;
  
  public ProcessEngineImpl getProcessEngine() {
    return processEngine;
  }
  
  public void setProcessEngine(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public ScopeInstance getParent() {
    return parent;
  }
  
  public void setParent(ScopeInstance parent) {
    this.parent = parent;
  }
  
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }
  
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public VariableDefinition getVariableDefinition() {
    return variableDefinition;
  }
  
  public void setVariableDefinition(VariableDefinition variableDefinition) {
    this.variableDefinition = variableDefinition;
  }

  @Override
  public void write(JsonWriter writer) {
    writer.writeObjectStart(this);
    writer.writeIdField(FIELD_VARIABLE_DEFINITION_ID, variableDefinition.getId());
    if (value!=null) {
      writer.writeFieldName(FIELD_VALUE);
      type.writeValue(writer, value);
    }
    writer.writeObjectEnd(this);
  }

  public static final String JSON_CONTEXT_KEY_UNRESOLVED_VARIABLE_INSTANCES = null;

  @Override
  public void read(JsonReader reader) {
    VariableDefinitionId variableDefinitionId = (VariableDefinitionId) reader.readId(FIELD_VARIABLE_DEFINITION_ID);
    Object valueJson = reader.getJsonObject(FIELD_VALUE);
    ScopeInstanceJsonReaderContext parentScopeContext = (ScopeInstanceJsonReaderContext) reader.getContext(ScopeInstance.JSON_READER_CONTEXT_KEY_SCOPE_INSTANCE);
    parentScopeContext.addUnresolvedVariableInstance(this, variableDefinitionId, valueJson);
  }
}

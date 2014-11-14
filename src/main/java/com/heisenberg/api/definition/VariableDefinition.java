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
package com.heisenberg.api.definition;

import com.heisenberg.spi.Type;



/**
 * @author Walter White
 */
public class VariableDefinition {

  public Object id;
  public String typeRefId;
  public Object initialValue;
  
  public static VariableDefinition type(String typeRefId) {
    VariableDefinition variableDefinition = new VariableDefinition();
    variableDefinition.typeRefId = typeRefId;
    return variableDefinition;
  }
  
  public static VariableDefinition type(Type type) {
    VariableDefinition variableDefinition = new VariableDefinition();
    variableDefinition.typeRefId = type.getId();
    return variableDefinition;
  }

  public VariableDefinition id(Object id) {
    this.id = id;
    return this;
  }
  
  public VariableDefinition initialValue(Object initialValue) {
    this.initialValue = initialValue;
    return this;
  }
}

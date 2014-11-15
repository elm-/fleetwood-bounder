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

  public String name;
  public String typeRefId;
  public Object initialValue;
  
  public static VariableDefinition type(String typeRefId) {
    VariableDefinition variableDefinition = new VariableDefinition();
    variableDefinition.typeRefId = typeRefId;
    return variableDefinition;
  }
  
  public static VariableDefinition type(Type type) {
    return type(type.getId());
  }

  /** The user defined name of the variable that can later be used 
   * for getting and setting variable values. */
  public VariableDefinition name(String name) {
    this.name = name;
    return this;
  }
  
  public VariableDefinition initialValue(Object initialValue) {
    this.initialValue = initialValue;
    return this;
  }
}

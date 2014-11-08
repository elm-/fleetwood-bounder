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

import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.type.Type;


/**
 * @author Walter White
 */
public class ParameterDefinition<T> {

  protected String name;
  protected Type<T> type;
  protected VariableDefinitionId variableDefinitionId;
  protected String expression;
  
  public ParameterDefinition() {
  }

  public ParameterDefinition(Type<T> type) {
    this.type = type;
  }
  
  public T get(ActivityInstance activityInstance) {
    return null;
  }

  public static <T> ParameterDefinition<T> type(Type<T> type) {
    return new ParameterDefinition<T>(type);
  }
  
  public ParameterDefinition<T> name(String name) {
    this.name = name;
    return this;
  }
  
  public ParameterDefinition<T> variableDefinitionId(VariableDefinitionId variableDefinitionId) {
    this.variableDefinitionId = variableDefinitionId;
    return this;
  }

  public ParameterDefinition<T> expression(String expression) {
    this.expression = expression;
    return this;
  }
  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public Type<T> getType() {
    return type;
  }

  
  public void setType(Type<T> type) {
    this.type = type;
  }

  
  public VariableDefinitionId getVariableDefinitionId() {
    return variableDefinitionId;
  }

  
  public void setVariableDefinitionId(VariableDefinitionId variableDefinitionId) {
    this.variableDefinitionId = variableDefinitionId;
  }

  
  public String getExpression() {
    return expression;
  }

  
  public void setExpression(String expression) {
    this.expression = expression;
  }

}

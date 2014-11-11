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


/**
 * @author Walter White
 */
public class ParameterValue {

  // one of the next 3 specifies the value
  protected Object object;
  protected VariableDefinition varableDefinition;
  protected String expression;
  
  public ParameterValue object(Object object) {
    this.object = object;
    return this;
  }
  
  public ParameterValue expression(String expression) {
    this.expression = expression;
    return this;
  }
  
  public ParameterValue variableDefinition(VariableDefinition variableDefinition) {
    this.varableDefinition = variableDefinition;
    return this;
  }

  public Object getObject() {
    return object;
  }
  
  public void setObject(Object object) {
    this.object = object;
  }
  
  public VariableDefinition getVarableDefinition() {
    return varableDefinition;
  }
  
  public void setVarableDefinition(VariableDefinition varableDefinition) {
    this.varableDefinition = varableDefinition;
  }
  
  public String getExpression() {
    return expression;
  }
  
  public void setExpression(String expression) {
    this.expression = expression;
  }
}

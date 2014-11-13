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
package com.heisenberg.expressions;

import java.util.Map;
import java.util.Set;

import javax.script.CompiledScript;

import com.heisenberg.definition.VariableDefinitionId;


/**
 * @author Walter White
 */
public class Expression {

  protected CompiledScript script;
  protected Map<String, VariableDefinitionId> variableDefinitionIds;
  protected Set<String> outputVariableNames;
  
  public Expression script(String script) {
    this.script = JavaScript.compile(script);
    return this;
  }
  
  public CompiledScript getScript() {
    return script;
  }
  
  public void setScript(CompiledScript script) {
    this.script = script;
  }
  
  public Map<String, VariableDefinitionId> getVariableDefinitionIds() {
    return variableDefinitionIds;
  }
  
  public void setVariableDefinitionIds(Map<String, VariableDefinitionId> variableDefinitionIds) {
    this.variableDefinitionIds = variableDefinitionIds;
  }
  
  public Set<String> getOutputVariableNames() {
    return outputVariableNames;
  }
  
  public void setOutputVariableNames(Set<String> outputVariableNames) {
    this.outputVariableNames = outputVariableNames;
  }
}

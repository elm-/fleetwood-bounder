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

package fleetwood.bounder.expressions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.ScopeInstance;


/**
 * @author Walter White
 */
public class ScriptInput {
  
  protected String language = "JavaScript";

  // the context containing the data
  protected ScopeInstance scopeInstance;
  
  // the script
  protected String script;
  
  // the list of variable values that needs to be extracted after the script is done
  protected Map<VariableDefinitionId, String> inputVariableNames;
  protected Set<String> outputVariableNames;
  
  public ScriptInput script(String script) {
    this.script = script;
    return this;
  }
  
  public ScriptInput inputVariableName(VariableDefinitionId variableDefinitionId, String inputVariableName) {
    if (inputVariableNames==null) {
      inputVariableNames = new HashMap<>();
    }
    this.inputVariableNames.put(variableDefinitionId, inputVariableName);
    return this;
  }
  
  public ScriptInput outputVariableName(String outputVariableName) {
    if (outputVariableNames==null) {
      outputVariableNames = new HashSet<>();
    }
    this.outputVariableNames.add(outputVariableName);
    return this;
  }
  
  public ScriptInput scopeInstance(ScopeInstance scopeInstance) {
    this.scopeInstance = scopeInstance;
    return this;
  }
  
  
  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public ScopeInstance getScopeInstance() {
    return scopeInstance;
  }

  
  public void setScopeInstance(ScopeInstance scopeInstance) {
    this.scopeInstance = scopeInstance;
  }

  public String getScript() {
    return script;
  }
  
  public void setScript(String script) {
    this.script = script;
  }
  
  public Set<String> getOutputVariableNames() {
    return outputVariableNames;
  }
  
  public void setOutputVariableNames(Set<String> scriptOutputVariables) {
    this.outputVariableNames = scriptOutputVariables;
  }

  public boolean hasScriptOutputVariables() {
    return outputVariableNames!=null && !outputVariableNames.isEmpty();
  }

  
  public Map<VariableDefinitionId, String> getInputVariableNames() {
    return inputVariableNames;
  }

  
  public void setInputVariableNames(Map<VariableDefinitionId, String> inputVariableNames) {
    this.inputVariableNames = inputVariableNames;
  }
  
  
}

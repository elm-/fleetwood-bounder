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

import java.util.HashMap;
import java.util.Map;

import com.heisenberg.definition.VariableDefinitionId;
import com.heisenberg.instance.ScopeInstance;


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
  protected Map<String, VariableDefinitionId> scriptVariableBindings;
  
  public ScriptInput script(String script) {
    this.script = script;
    return this;
  }
  
  public ScriptInput scriptVariableBinding(String scriptVariableName, VariableDefinitionId variableDefinitionId) {
    if (scriptVariableBindings==null) {
      scriptVariableBindings = new HashMap<>();
    }
    this.scriptVariableBindings.put(scriptVariableName, variableDefinitionId);
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

  public Map<String, VariableDefinitionId> getScriptVariableBindings() {
    return scriptVariableBindings;
  }
  
  public void setScriptVariableBindings(Map<String, VariableDefinitionId> scriptVariableBindings) {
    this.scriptVariableBindings = scriptVariableBindings;
  }
}

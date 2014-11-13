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
package com.heisenberg.bpmn.activities;

import java.util.Map;

import javax.script.CompiledScript;

import com.heisenberg.definition.ActivityDefinition;
import com.heisenberg.definition.VariableDefinitionId;
import com.heisenberg.instance.ActivityInstance;


/**
 * @author Walter White
 */
public class ScriptTask extends ActivityDefinition {
  
  protected CompiledScript compiledScript;
  protected Map<String, VariableDefinitionId> variableDefinitionIds;

  @Override
  public void start(ActivityInstance activityInstance) {
    // clone the variable values
    // invoke javascript
    // perform dirty checking on the variables
  }

}

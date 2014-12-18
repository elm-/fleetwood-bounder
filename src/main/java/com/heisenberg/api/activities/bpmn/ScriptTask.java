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
package com.heisenberg.api.activities.bpmn;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.definition.ActivityDefinition;
import com.heisenberg.impl.script.Script;
import com.heisenberg.impl.script.ScriptResult;
import com.heisenberg.impl.script.ScriptService;
import com.heisenberg.plugin.Validator;
import com.heisenberg.plugin.activities.AbstractActivityType;
import com.heisenberg.plugin.activities.ControllableActivityInstance;


/**
 * @author Walter White
 */
@JsonTypeName("serviceTask")
public class ScriptTask extends AbstractActivityType {

  public String script;
  public Map<String, String> scriptToProcessMappings;
  public Object resultVariableDefinitionId;
  
  @JsonIgnore
  public Script compiledScript;
  
  @Override
  public void validate(ActivityDefinition activityDefinition, Validator validator) {
    if (script!=null) {
      ScriptService scriptService = validator.getScriptService();
      compiledScript = scriptService.compile(script);
      compiledScript.scriptToProcessMappings = scriptToProcessMappings;
    }
    // TODO if specified, check if the resultVariableDefinitionId exists
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    if (script!=null) {
      ScriptService scriptService = activityInstance.getScriptService();
      ScriptResult scriptResult = scriptService.evaluateScript(activityInstance, compiledScript);
      scriptResult.getResult();
      /* Object result = 
        if (resultVariableDefinitionId!=null) {
        activityInstance.setVariableValue(resultVariableDefinitionId, result);
      } */
    }
    activityInstance.onwards();
  }
}

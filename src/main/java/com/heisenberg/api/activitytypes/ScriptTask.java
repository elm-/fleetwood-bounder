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
package com.heisenberg.api.activitytypes;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.api.definition.Activity;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.impl.plugin.AbstractActivityType;
import com.heisenberg.impl.plugin.ControllableActivityInstance;
import com.heisenberg.impl.plugin.Validator;
import com.heisenberg.impl.script.Script;
import com.heisenberg.impl.script.ScriptResult;
import com.heisenberg.impl.script.ScriptService;


/**
 * @author Walter White
 */
@JsonTypeName("serviceTask")
public class ScriptTask extends AbstractActivityType {

  @JsonIgnore
  protected ScriptService scriptService;
  public String script;
  public Map<String, String> scriptToProcessMappings;
  public Object resultVariableDefinitionId;
  
  @JsonIgnore
  public Script compiledScript;
  
  @Override
  public void validate(Activity activity, Validator validator) {
    if (script!=null) {
      this.scriptService = validator.getServiceRegistry().getService(ScriptService.class);
      this.compiledScript = scriptService.compile(script);
      this.compiledScript.scriptToProcessMappings = scriptToProcessMappings;
    }
    // TODO if specified, check if the resultVariableDefinitionId exists
  }

  @Override
  public void start(ControllableActivityInstance activityInstance) {
    if (script!=null) {
      ScriptResult scriptResult = scriptService.evaluateScript(activityInstance, compiledScript);
      scriptResult.getResult();
      /* Object result = 
        if (resultVariableDefinitionId!=null) {
        activityInstance.setVariableValue(resultVariableDefinitionId, result);
      } */
    }
    activityInstance.onwards();
  }
  
  @Override
  public boolean isAsync(ActivityInstance activityInstance) {
    return true;
  }
}

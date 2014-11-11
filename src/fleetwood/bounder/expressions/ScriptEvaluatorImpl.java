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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * @author Walter White
 */
public class ScriptEvaluatorImpl implements ScriptEvaluator {

  @Override
  public ScriptOutput evaluateScript(ScriptInput scriptInput) {
    ScriptEngine engine = new ScriptEngineManager().getEngineByName(scriptInput.getLanguage());
    ScriptOutput scriptOutput = new ScriptOutput(scriptInput);
    try {
      ScriptContextImpl scriptContext = new ScriptContextImpl(scriptInput, scriptOutput);
      engine.setContext(scriptContext);
      Object result = engine.eval(scriptInput.getScript());
      scriptOutput.setResult(result);
      if (scriptInput.hasScriptOutputVariables()) {
        for (String scriptOutputVariable: scriptInput.getOutputVariableNames()) {
          Object value = engine.get(scriptOutputVariable);
          scriptOutput.setOutputVariable(scriptOutputVariable, value);
        }
      }
    } catch (ScriptException e) {
      scriptOutput.setException(e);
    }
    return scriptOutput;
  }
}

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

import java.io.StringWriter;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


/**
 * @author Walter White
 */
public class JavaScript implements ScriptEvaluator {
  
  static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

  public static Expression buildExpression(String expressionText) {
    Expression expression = new Expression();
    expression.setScript(compile(expressionText));
    return expression;
  }

  public static CompiledScript compile(String script) {
    try {
      return ((Compilable)getScriptEngine()).compile(script);
    } catch (ScriptException e) {
      throw new RuntimeException(e);
    }
  }

  public static ScriptEngine getScriptEngine() {
    return scriptEngineManager.getEngineByName(getScriptLanguage());
  }

  public static String getScriptLanguage() {
    return "JavaScript";
  }

  @Override
  public ScriptOutput evaluateScript(ScriptInput scriptInput) {
    ScriptEngine engine = new ScriptEngineManager().getEngineByName(scriptInput.getLanguage());
    ScriptOutput scriptOutput = new ScriptOutput(engine);
    try {
      StringWriter logWriter = new StringWriter();
      ScriptContextImpl scriptContext = new ScriptContextImpl(scriptInput.scopeInstance, scriptInput.scriptVariableBindings, logWriter);
      engine.setContext(scriptContext);
      Object result = engine.eval(scriptInput.getScript());
      scriptOutput.setResult(result);
      scriptOutput.setLogs(logWriter.toString());
    } catch (ScriptException e) {
      scriptOutput.setException(e);
    }
    return scriptOutput;
  }
}

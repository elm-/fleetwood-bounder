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
import java.util.Map;


/**
 * @author Walter White
 */
public class ScriptOutput {

  protected ScriptInput scriptInput;
  protected Object result;
  protected Map<String,Object> outputVariables;
  protected Throwable exception;
  protected String logs;
  
  public ScriptOutput(ScriptInput scriptInput) {
    this.scriptInput = scriptInput;
  }

  public ScriptInput getScriptInput() {
    return scriptInput;
  }
  
  public void setScriptInput(ScriptInput scriptInput) {
    this.scriptInput = scriptInput;
  }
  
  public Object getResult() {
    return result;
  }
  
  public void setResult(Object result) {
    this.result = result;
  }
  
  public Map<String, Object> getOutputVariables() {
    return outputVariables;
  }
  
  public void setOutputVariables(Map<String, Object> outputVariables) {
    this.outputVariables = outputVariables;
  }
  
  public Throwable getException() {
    return exception;
  }
  
  public void setException(Throwable exception) {
    this.exception = exception;
  }

  public void setOutputVariable(String variableName, Object value) {
    if (outputVariables==null) {
      outputVariables = new HashMap<>();
    }
    outputVariables.put(variableName, value);
  }
  
  public String getLogs() {
    return logs;
  }
  
  public void setLogs(String logs) {
    this.logs = logs;
  }
}

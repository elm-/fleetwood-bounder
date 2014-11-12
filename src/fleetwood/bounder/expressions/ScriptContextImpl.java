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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fleetwood.bounder.ProcessEngine;
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.ScopeInstance;


/**
 * @author Walter White
 */
public class ScriptContextImpl extends SimpleScriptContext {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);
  
  ScriptInput scriptInput;
  ScriptOutput scriptOutput;
  
  ScopeInstance scopeInstance;
  
  Writer writer = new StringWriter();
  
  public ScriptContextImpl(ScriptInput scriptInput, ScriptOutput scriptOutput) {
    this.scriptInput = scriptInput;
    this.scriptOutput = scriptOutput;
    this.scopeInstance = scriptInput.getScopeInstance();
    setWriter(writer);
    setErrorWriter(writer);
    Map<VariableDefinitionId, String> inputVariableNames = scriptInput.getInputVariableNames();
    if (inputVariableNames!=null) {
      for (VariableDefinitionId variableDefinitionId: inputVariableNames.keySet()) {
        String scriptVariableName = inputVariableNames.get(variableDefinitionId);
        Object value = scopeInstance.getVariableValueRecursive(variableDefinitionId);
        setAttribute(scriptVariableName, value, ENGINE_SCOPE);
      }
    }
  }

//  public void setVariableValue(String scriptVariableName, Object scriptValue) {
//    if (variableValues==null) {
//      variableValues = new SimpleBindings();
//    }
//    variableValues.put(scriptVariableName, scriptValue);
//  }
//  
//  @Override
//  public void setAttribute(String name, Object value, int scope) {
//    log.debug("invoked setAttribute "+name+" "+value+" "+scope);
//    if (scope==ENGINE_SCOPE) {
//      variableValues.put(name, value);
//    }
//  }
//
//  @Override
//  public Object getAttribute(String name, int scope) {
//    log.debug("invoked getAttribute "+name+" "+scope);
//    if (scope==ENGINE_SCOPE) {
//      if (hasVariableValue(name)) {
//        return variableValues.get(name);
//      }
//    }
//    return null;
//  }
//  
//  @Override
//  public int getAttributesScope(String name) {
//    log.debug("invoked getAttributesScope "+name);
//    if (hasVariableValue(name)) {
//        return ENGINE_SCOPE;
//    }
//    return -1;
//  }
//
//  @Override
//  public Bindings getBindings(int scope) {
//    log.debug("invoked getBindings "+scope);
//    if (scope==ENGINE_SCOPE) {
//      return variableValues;
//    }
//    return null;
//  }
//
//  @Override
//  public void setBindings(Bindings bindings, int scope) {
//    log.debug("invoked setBindings "+bindings+" "+scope);
//  }
//
//  @Override
//  public Object removeAttribute(String name, int scope) {
//    log.debug("invoked getAttribute "+name+" "+scope);
//    return null;
//  }
//
//  @Override
//  public Object getAttribute(String name) {
//    log.debug("invoked getAttribute "+name);
//    return null;
//  }
//
//  private boolean hasVariableValue(String name) {
//    return variableValues!=null && variableValues.containsKey(name);
//  }
//
//  @Override
//  public Writer getWriter() {
//    log.debug("invoked getWriter");
//    return writer;
//  }
//
//  @Override
//  public Writer getErrorWriter() {
//    log.debug("invoked getErrorWriter ");
//    return writer;
//  }
//
//  @Override
//  public void setWriter(Writer writer) {
//    log.debug("invoked setWriter "+writer);
//  }
//
//  @Override
//  public void setErrorWriter(Writer writer) {
//    log.debug("invoked setErrorWriter "+writer);
//  }
//
//  @Override
//  public Reader getReader() {
//    log.debug("invoked getReader ");
//    return null;
//  }
//
//  @Override
//  public void setReader(Reader reader) {
//    log.debug("invoked setReader "+reader);
//  }
//
//  @Override
//  public List<Integer> getScopes() {
//    log.debug("invoked getScopes ");
//    return null;
//  }

}

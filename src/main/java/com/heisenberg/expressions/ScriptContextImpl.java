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

import java.io.Writer;
import java.util.Map;

import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.definition.VariableDefinitionId;
import com.heisenberg.instance.ScopeInstanceImpl;


/**
 * @author Walter White
 */
public class ScriptContextImpl extends SimpleScriptContext {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);
  
  public ScriptContextImpl(ScopeInstanceImpl scopeInstance, Map<String,VariableDefinitionId> scriptVariableBindings, Writer logWriter) {
    setWriter(logWriter);
    setErrorWriter(logWriter);
    setBindings(new ScriptBindings(scopeInstance, scriptVariableBindings, logWriter), ENGINE_SCOPE);
  }

}

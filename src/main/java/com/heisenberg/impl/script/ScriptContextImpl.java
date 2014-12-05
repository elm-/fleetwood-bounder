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
package com.heisenberg.impl.script;

import java.io.Writer;

import javax.script.SimpleScriptContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.configuration.Script;
import com.heisenberg.impl.instance.ScopeInstanceImpl;


/**
 * @author Walter White
 */
public class ScriptContextImpl extends SimpleScriptContext {
  
  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);
  
  public ScriptContextImpl(ScopeInstanceImpl scopeInstance, Script script, Writer logWriter) {
    setWriter(logWriter);
    setErrorWriter(logWriter);
    setBindings(new ScriptBindings(script, scopeInstance, logWriter), ENGINE_SCOPE);
  }

}

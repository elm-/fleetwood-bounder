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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.impl.instance.ScopeInstanceImpl;
import com.heisenberg.impl.plugin.TypedValue;
import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public class ScriptBindings implements Bindings {
  
  public static final Logger log = LoggerFactory.getLogger(ScriptBindings.class);
  
  protected Map<String,String> scriptToProcessMappings;
  protected String language;
  protected ScopeInstanceImpl scopeInstance;
  protected Console console;

  public ScriptBindings(Script script, ScopeInstanceImpl scopeInstance, Writer logWriter) {
    this.scriptToProcessMappings = script.scriptToProcessMappings;
    this.language = script.language;
    this.scopeInstance = scopeInstance;
    this.console = new Console(logWriter);
  }
  
  @Override
  public boolean containsKey(Object key) {
    // log.debug("ScriptBindings.containsKey("+key+")");
    if (!(key instanceof String)) {
      return false;
    }
    String name = (String) key;
    if (isIgnored(name)) {
      return false;
    }
    if ("console".equals(name)) {
      return true;
    }
    if (scriptToProcessMappings!=null && scriptToProcessMappings.containsKey(name)) {
      return true;
    }
    if (name.length()>0) {
      return scopeInstance.getScopeDefinition().containsVariable(name);
    }
    return false;
  }

  @Override
  public Object get(Object key) {
    // log.debug("ScriptBindings.get("+key+")");
    if (!(key instanceof String)) {
      return null;
    }
    String scriptVariableName = (String) key;
    if ("console".equals(scriptVariableName)) {
      return console;
    }
    TypedValue typedValue = getTypedValue(scriptVariableName);
    DataType dataType = typedValue.getType();
    Object value = typedValue.getValue();
    return dataType.convertInternalToScriptValue(value, language);
  }
  
  protected String getVariableDefinitionId(String scriptVariableId) {
    if (scriptToProcessMappings!=null) {
      String variableDefinitionId = scriptToProcessMappings.get(scriptVariableId);
      if (variableDefinitionId!=null) {
        return variableDefinitionId;
      }
    }
    return scriptVariableId;
  }

  public TypedValue getTypedValue(String scriptVariableName) {
    Object variableDefinitionId = getVariableDefinitionId(scriptVariableName);
    return scopeInstance.getVariableTypedValue(variableDefinitionId);
  }

  static final Map<String, List<String>> NAME_TO_IGNORE = new HashMap<>();
  static {
    NAME_TO_IGNORE.put(ScriptServiceImpl.JAVASCRIPT, Arrays.asList("context", "print", "println"));
  }
  protected boolean isIgnored(String scriptVariableName) {
    List<String> namesToIgnore = NAME_TO_IGNORE.get(language);
    if (namesToIgnore!=null && namesToIgnore.contains(scriptVariableName)) {
      return true;
    }
    return false;
  }
  
  @Override
  public Object put(String scriptVariableName, Object scriptValue) {
    // log.debug("ScriptBindings.put("+scriptVariableName+","+scriptValue+")");
    if (isIgnored(scriptVariableName)){
      return null;
    }
    TypedValue typedValue = getTypedValue(scriptVariableName);
    if (typedValue!=null) {
      String variableDefinitionId = getVariableDefinitionId(scriptVariableName);
      DataType dataType = typedValue.getType();
      Object value = dataType.convertScriptValueToInternal(scriptValue, language);
      scopeInstance.setVariableValue(variableDefinitionId, value);
    }
    return null;
  }

  // --- dungeons -------------------------------------------------------------------------

  @Override
  public int size() {
    log.debug("ScriptBindings.size()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public boolean isEmpty() {
    log.debug("ScriptBindings.isEmpty()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public boolean containsValue(Object value) {
    log.debug("ScriptBindings.containsValue("+value+")");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public void clear() {
    log.debug("ScriptBindings.clear()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Set<String> keySet() {
    log.debug("ScriptBindings.keySet()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Collection<Object> values() {
    log.debug("ScriptBindings.values()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    log.debug("ScriptBindings.entrySet()");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public void putAll(Map< ? extends String, ? extends Object> toMerge) {
    log.debug("ScriptBindings.putAll("+toMerge+")");
    throw new UnsupportedOperationException("Please implement me");
  }

  @Override
  public Object remove(Object key) {
    log.debug("ScriptBindings.remove("+key+")");
    throw new UnsupportedOperationException("Please implement me");
  }
}

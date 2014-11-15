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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.instance.ScopeInstanceImpl;
import com.heisenberg.spi.Type;
import com.heisenberg.type.TypedValue;


/**
 * @author Walter White
 */
public class ScriptBindings implements Bindings {
  
  public static final Logger log = LoggerFactory.getLogger(ScriptBindings.class);
  
  protected ScopeInstanceImpl scopeInstance;
  protected Map<String,VariableDefinitionId> scriptVariableBindings;
  protected Console console;

  public ScriptBindings(ScopeInstanceImpl scopeInstance, Map<String,VariableDefinitionId> scriptVariableBindings, Writer logWriter) {
    this.scopeInstance = scopeInstance;
    this.scriptVariableBindings = scriptVariableBindings;
    this.console = new Console(logWriter);
  }
  
  @Override
  public boolean containsKey(Object key) {
    log.debug("ScriptBindings.containsKey("+key+")");
    if (!(key instanceof String)) {
      return false;
    }
    String name = (String) key;
    if (NAME_TO_IGNORE.contains(name)) {
      return false;
    }
    if ("console".equals(name)) {
      return true;
    }
    if (scriptVariableBindings!=null && scriptVariableBindings.containsKey(name)) {
      return true;
    }
    if (name.length()>0) {
      return scopeInstance.getScopeDefinition().containsVariable(new VariableDefinitionId(name));
    }
    return false;
  }

  @Override
  public Object get(Object key) {
    log.debug("ScriptBindings.get("+key+")");
    if (!(key instanceof String)) {
      return null;
    }
    String name = (String) key;
    if ("console".equals(name)) {
      return console;
    }
    VariableDefinitionId variableDefinitionId = getVariableDefinitionId(name);
    if (variableDefinitionId!=null) {
      TypedValue typedValue = scopeInstance.getVariableValueRecursive(variableDefinitionId);
      Type type = typedValue.getType();
      Object value = typedValue.getValue();
      return type.convertValueToJavaScript(value);
    }
    return null;
  }
  
  public TypedValue getTypedValue(String name) {
    VariableDefinitionId variableDefinitionId = getVariableDefinitionId(name);
    return getTypedValue(variableDefinitionId);
  }

  private TypedValue getTypedValue(VariableDefinitionId variableDefinitionId) {
    if (variableDefinitionId!=null) {
      return scopeInstance.getVariableValueRecursive(variableDefinitionId);
    }
    return null;
  }

  private VariableDefinitionId getVariableDefinitionId(String name) {
    VariableDefinitionId variableDefinitionId = null;
    if (scriptVariableBindings!=null && scriptVariableBindings.containsKey(name)) {
      variableDefinitionId = scriptVariableBindings.get(name);
    }
    if (name.length()>0) {
      variableDefinitionId = new VariableDefinitionId(name);
      if (!scopeInstance.getScopeDefinition().containsVariable(variableDefinitionId)) {
        variableDefinitionId = null;
      }
    }
    return variableDefinitionId;
  }

  static final List<String> NAME_TO_IGNORE = Arrays.asList(
          "context", "print", "println");
  
  @Override
  public Object put(String name, Object javaScriptValue) {
    log.debug("ScriptBindings.put("+name+","+javaScriptValue+")");
    if (NAME_TO_IGNORE.contains(name)) {
      return null;
    }
    VariableDefinitionId variableDefinitionId = getVariableDefinitionId(name);
    TypedValue typedValue = getTypedValue(variableDefinitionId);
    if (typedValue!=null) {
      Type type = typedValue.getType();
      Object value = type.convertJavaScriptToValue(javaScriptValue);
      scopeInstance.setVariableValueRecursive(variableDefinitionId, value);
    }
    return null;
  }

  // ------------------------------------------------------------------------------------

  @Override
  public int size() {
    log.debug("ScriptBindings.size()");
    return 0;
  }

  @Override
  public boolean isEmpty() {
    log.debug("ScriptBindings.isEmpty()");
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    log.debug("ScriptBindings.containsValue("+value+")");
    return false;
  }

  @Override
  public void clear() {
    log.debug("ScriptBindings.clear()");
  }

  @Override
  public Set<String> keySet() {
    log.debug("ScriptBindings.keySet()");
    return null;
  }

  @Override
  public Collection<Object> values() {
    log.debug("ScriptBindings.values()");
    return null;
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    log.debug("ScriptBindings.entrySet()");
    return null;
  }

  @Override
  public void putAll(Map< ? extends String, ? extends Object> toMerge) {
    log.debug("ScriptBindings.putAll("+toMerge+")");
  }

  @Override
  public Object remove(Object key) {
    log.debug("ScriptBindings.remove("+key+")");
    return null;
  }
}

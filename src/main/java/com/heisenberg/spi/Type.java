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
package com.heisenberg.spi;

import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.type.ProcessDefinitionIdType;
import com.heisenberg.type.TextType;


/**
 * @author Walter White
 */
public abstract class Type implements Spi {
  
  public static final TextType TEXT = new TextType();
  public static final Type PROCESS_DEFINITION_ID = new ProcessDefinitionIdType();
  
  
  protected ProcessEngineImpl processEngine;
  
  public abstract String getId();

  public abstract Object convertApiToInternalValue(Object apiValue) throws InvalidApiValueException;

  public Object convertInternalToApiValue(Object internalValue) {
    return internalValue;
  }

  public Object convertInternalToScriptValue(Object internalValue, String language) {
    return internalValue;
  }

  public Object convertScriptValueToInternal(Object scriptValue, String language) {
    return scriptValue;
  }
}

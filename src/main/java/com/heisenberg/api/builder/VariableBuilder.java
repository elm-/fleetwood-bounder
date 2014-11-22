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
package com.heisenberg.api.builder;

import com.heisenberg.api.type.DataType;



/**
 * @author Walter White
 */
public interface VariableBuilder {

  /** The user defined name of the variable that can later be used 
   * for getting and setting variable values. */
  VariableBuilder name(String name);
  
  VariableBuilder type(String typeRefId);
  
  VariableBuilder dataType(DataType dataType);
  
  VariableBuilder dataTypeJavaBean(Class<?> userDefinedJavaBeanClass);

  VariableBuilder dataTypeId(String dataTypeId);

  VariableBuilder initialValue(Object initialValue);
  
  VariableBuilder initialValueJson(Object initialValueJson);
  
  VariableBuilder line(Long lineNumber);
  
  VariableBuilder column(Long columnNumber);
}

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
package com.heisenberg.form;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class FormField {

  public String id;
  public String label;
  public Boolean required;
  public Boolean readOnly;
  
  /** Refers to a type in the processEngineImpl.types.
   * Either typeId or type must be specified. */
  public String typeId;

  /** A configured type defined inline.
   * This type could have been copied from the process definition variable declaration. */
  public Type type; 
  
  @JsonIgnore
  public Object value;
  public Object jsonValue;
  
}

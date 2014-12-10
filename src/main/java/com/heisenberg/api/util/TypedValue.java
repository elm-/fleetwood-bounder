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
package com.heisenberg.api.util;

import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public class TypedValue {

  DataType dataType;
  Object value;

  public TypedValue(DataType dataType, Object value) {
    this.dataType = dataType;
    this.value = value;
  }

  public DataType getType() {
    return dataType;
  }
  
  public void setType(DataType dataType) {
    this.dataType = dataType;
  }
  
  public Object getValue() {
    return value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
}

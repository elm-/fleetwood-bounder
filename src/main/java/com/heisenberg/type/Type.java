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
package com.heisenberg.type;




/**
 * @author Walter White
 */
public abstract class Type {
  
  public static final TextType TEXT = new TextType();
  public static final IdType ID = new IdType();
  
  /** returns the javascript object for the given value.
   * @param value is the internal java object representation used in the process engine.
   *   value may be null. 
   * @return the java script value */
  public Object convertValueToJavaScript(Object value) {
    return value;
  }

  /** returns the value for the given javaScriptValue.
   * @param javaScriptValue may be null.
   * @return is the internal java object representation used in the process engine. */
  public Object convertJavaScriptToValue(Object javaScriptValue) {
    return javaScriptValue;
  }
}

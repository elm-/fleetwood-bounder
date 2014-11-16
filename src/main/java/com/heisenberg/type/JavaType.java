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

import java.util.Map;

import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class JavaType extends Type {
  
  Class<?> javaClass;
  
  public JavaType(Class< ? > javaClass) {
    this.javaClass = javaClass;
  }

  @Override
  public String getId() {
    return javaClass.getName();
  }

  @Override
  public Object convertApiToInternalValue(Object apiValue) throws InvalidApiValueException {
    if (apiValue==null) return null;
    if (javaClass.isAssignableFrom(apiValue.getClass())) {
      return apiValue;
    }
    if (Map.class.isAssignableFrom(apiValue.getClass())) {
      
    }
    return null;
  }

}

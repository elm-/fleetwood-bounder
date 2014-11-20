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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.heisenberg.spi.Binding;
import com.heisenberg.spi.Label;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class ObjectField {

  public String name;
  public String label;
  public String typeId;
  
  public ObjectField(Field javaField) {
    this.name = javaField.getName();
    Label label = javaField.getAnnotation(Label.class);
    this.label = label!=null ? label.value() : null;
    Class<?> fieldType = javaField.getType();
    if (String.class == fieldType) {
      typeId = Type.TEXT.getId();
    } else if (Binding.class == fieldType) {
      java.lang.reflect.Type javaFieldType = javaField.getGenericType();
      java.lang.reflect.Type targetType = null;
      if (javaFieldType instanceof ParameterizedType) {
        ParameterizedType parametrizedType = (ParameterizedType) javaFieldType;
        java.lang.reflect.Type[] actualTypeArguments = parametrizedType.getActualTypeArguments();
        if (actualTypeArguments.length==1) {
          targetType = actualTypeArguments[0];
        }
      }
      if (targetType==null) {
        throw new RuntimeException("Expected single generic type binding for a Binding field");
      }
      typeId = new BindingType(targetType).getId();
    }
  }
}

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
package com.heisenberg.api;

import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.type.NumberType;
import com.heisenberg.impl.type.TextType;


/**
 * @author Walter White
 */
public interface DataTypes {

  TextType TEXT = new TextType();
  NumberType NUMBER = new NumberType();

  /** create a list data type from the element data type used to set in {@link #dataType(DataType)} */
  DataType list(DataType elementDataType);
  /** create a javabean data type used to set in {@link #dataType(DataType)} */
  DataType javaBean(Class<?> javaBeanClass);
}

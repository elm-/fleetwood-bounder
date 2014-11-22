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

import com.heisenberg.spi.AbstractDataType;
import com.heisenberg.spi.DataType;
import com.heisenberg.spi.InvalidApiValueException;


/**
 * @author Walter White
 */
public class BindingType extends AbstractDataType {
  
  String id;
  DataType dataType;

  public BindingType(DataType dataType) {
    this.id = "binding<"+dataType.getId()+">";
    this.dataType = dataType;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getLabel() {
    return dataType.getLabel()+" binding";
  }

  @Override
  public Object convertJsonToInternalValue(Object apiValue) throws InvalidApiValueException {
    throw new UnsupportedOperationException("TODO");
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    throw new UnsupportedOperationException("TODO");
  }
 
}

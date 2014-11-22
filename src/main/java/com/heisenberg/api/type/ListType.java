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
package com.heisenberg.api.type;

import java.util.List;


/**
 * @author Walter White
 */
public class ListType extends AbstractDataType implements DataType {
  
  DataType dataType;
  String id;
  
  /** constructor for json, dataType is a required field. */
  protected ListType() {
  }

  public ListType(DataType dataType) {
    this.dataType = dataType;
    this.id = "list<"+dataType.getId()+">";
  }

  @Override
  public String getLabel() {
    return null;
  }

  @Override
  public String getId() {
    return id;
  }
  
  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if (internalValue==null) {
      return;
    }
    if (!(internalValue instanceof List)) {
      throw new InvalidValueException("Value for "+id+" must be a list, but was "+internalValue+" ("+internalValue.getClass().getName()+")");
    }
    @SuppressWarnings("unchecked")
    List<Object> list = (List<Object>) internalValue;
    for (Object element: list) {
      dataType.validateInternalValue(element);
    }
  }

  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    if (jsonValue==null) {
      return null;
    }
    if (!(jsonValue instanceof List)) {
      throw new InvalidValueException("Json value for "+id+" must be a list, but was "+jsonValue+" ("+jsonValue.getClass().getName()+")");
    }
    @SuppressWarnings("unchecked")
    List<Object> list = (List<Object>) jsonValue;
    for (int i=0; i<list.size(); i++) {
      Object elementJsonValue = list.get(i);
      Object elementInternalValue = dataType.convertJsonToInternalValue(elementJsonValue);
      list.set(i, elementInternalValue);
    }
    return list;
  }

  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    if (internalValue==null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    List<Object> list = (List<Object>) internalValue;
    for (int i=0; i<list.size(); i++) {
      Object elementInternalValue = list.get(i);
      Object elementJsonValue = dataType.convertInternalToJsonValue(elementInternalValue);
      list.set(i, elementJsonValue);
    }
    return list;
  }
}

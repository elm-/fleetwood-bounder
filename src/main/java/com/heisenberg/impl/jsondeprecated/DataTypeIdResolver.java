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
package com.heisenberg.impl.jsondeprecated;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.heisenberg.impl.plugin.DataTypes;

/**
 * @author Walter White
 */
public class DataTypeIdResolver implements TypeIdResolver {

  protected DataTypes dataTypes;
  protected JavaType baseType;

  public DataTypeIdResolver(DataTypes dataTypes) {
    this.dataTypes = dataTypes;
  }

  @Override
  public void init(JavaType baseType) {
    this.baseType = baseType;
  }

  @Override
  public Id getMechanism() {
    return Id.CUSTOM;
  }

  @Override
  public String idFromValue(Object obj) {
    return idFromValueAndType(obj, obj.getClass());
  }

  @Override
  public String idFromBaseType() {
    return idFromValueAndType(null, baseType.getRawClass());
  }

  @Override
  public String idFromValueAndType(Object obj, Class< ? > clazz) {
    String type = dataTypes.getType(clazz);
    if (type==null) {
      throw new RuntimeException("Couldn't find type for data type "+clazz.getName());
    }
    return type;
  }

  @Override
  public JavaType typeFromId(String type) {
    Class< ? > clazz = dataTypes.getDataTypeClazz(type);
    if (clazz==null) {
      throw new RuntimeException("Couldn't find class for data type "+type);
    }
    return TypeFactory.defaultInstance().constructSpecializedType(baseType, clazz);
  }
}

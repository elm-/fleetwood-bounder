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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Type;
import com.heisenberg.util.Reflection;


/**
 * @author Walter White
 */
public class ObjectType extends Type {

  protected String id; // the id is the fully qualified java class name
  protected List<ObjectField> fields;

  @Override
  public Object convertJsonToInternalValue(Object apiValue) throws InvalidApiValueException {
    return null;
  }

  @Override
  public String getLabel() {
    return null;
  }

  public ObjectType(Class< ? > type) {
    scanFields(type);
  }

  void scanFields(Class< ? > type) {
    List<Field> javaFields = Reflection.getFieldsRecursive(type, Reflection.NOT_STATIC);
    if (javaFields.isEmpty()) {
      fields = new ArrayList<ObjectField>(javaFields.size());
      for (Field javaField: javaFields) {
        if (!Modifier.isStatic(javaField.getModifiers())) {
          addField(new ObjectField(javaField));
        }
      }
    }
  }

  void addField(ObjectField field) {
    if (fields==null) {
      fields = new ArrayList<>();
    }
    fields.add(field);
  }

  public String getId() {
    return id;
  }
}

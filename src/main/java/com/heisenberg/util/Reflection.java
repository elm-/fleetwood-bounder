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
package com.heisenberg.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.Spi;


/**
 * @author Walter White
 */
public class Reflection {
  
  public interface FieldCondition {
    boolean meets(Field field);
  }
  
  public static final FieldCondition STATIC = new FieldCondition() {
    public boolean meets(Field field) {
      return Modifier.isStatic(field.getModifiers());
    }
  };
  
  public static final FieldCondition NOT_STATIC = new FieldCondition() {
    public boolean meets(Field field) {
      return !Modifier.isStatic(field.getModifiers());
    }
  };
  
  public static final FieldCondition PUBLIC = new FieldCondition() {
    public boolean meets(Field field) {
      return Modifier.isPublic(field.getModifiers());
    }
  };

  public static List<Field> getFieldsRecursive(Class< ? > type, FieldCondition... fieldConditions) {
    List<Field> fieldCollector = new ArrayList<>();
    collectFieldsRecursive(type, fieldConditions, fieldCollector);
    return fieldCollector;
  }

  static void collectFieldsRecursive(Class< ? > type, FieldCondition[] fieldConditions, List<Field> fieldCollector) {
    Field[] fields = type.getDeclaredFields();
    if (fields!=null) {
      for (Field field: fields) {
        if (meetsConditions(field, fieldConditions)) {
          fieldCollector.add(field);
        }
      }
    }
    Class< ? > superclass = type.getSuperclass();
    if (superclass!=null && superclass!=Object.class) {
      collectFieldsRecursive(superclass, fieldConditions, fieldCollector);
    }
  }

  static boolean meetsConditions(Field field, FieldCondition[] fieldConditions) {
    if (fieldConditions!=null) {
      for (FieldCondition condition: fieldConditions) {
        if (!condition.meets(field)) {
          return false;
        }
      }
    }
    return true;
  }

  public static <T> T newInstance(Class<T> type) {
    if (type==null) {
      return null;
    }
    try {
      return type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+type+" with the default constructor: "+e.getMessage(), e);
    }
  }
}

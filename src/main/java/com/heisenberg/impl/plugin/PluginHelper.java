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
package com.heisenberg.impl.plugin;

import java.lang.reflect.Constructor;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.impl.util.Reflection;


/**
 * @author Walter White
 */
public class PluginHelper {

  protected static String getJsonTypeName(Object o) {
    JsonTypeName jsonTypeName = o.getClass().getAnnotation(JsonTypeName.class);
    return jsonTypeName!=null ? jsonTypeName.value() : null;
  }

  protected static boolean couldBeConfigured(Object o) {
    Class< ? extends Object> clazz = o.getClass();
    return !hasDefaultConstructor(clazz)
           && !Reflection.getNonStaticFieldsRecursive(clazz).isEmpty();
  }

//  /** returns true if the object o has the same member field values as an object that is created 
//   * with the class' default constructor. */
//  protected static boolean isDefaultConstructed(Object o) {
//    try {
//      Class<?> clazz = o.getClass();
//      if (!hasDefaultConstructor(clazz)) {
//        return false;
//      }
//      return isDefaultConstructed(clazz, o, clazz.newInstance());
//    } catch (Exception e) {
//      return true;
//    }
//  }

  protected static boolean hasDefaultConstructor(Class<?> clazz) {
    for (Constructor<?> constructor: clazz.getDeclaredConstructors()) {
      Class< ? >[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes==null || parameterTypes.length==0) {
        return true;
      }
    }
    return false;
  }

//  protected static boolean isDefaultConstructed(Class<?> clazz, Object o, Object defaultConstructed) throws Exception {
//    for (Field field: clazz.getDeclaredFields()) {
//      if (!Modifier.isStatic(field.getModifiers())) {
//        Object oValue = field.get(o);
//        Object defaultConstructedValue = field.get(defaultConstructed);
//        if ( (oValue==null && defaultConstructedValue!=null)
//             || (oValue!=null && defaultConstructedValue==null)
//             || (oValue!=null && !oValue.equals(defaultConstructedValue)) ) {
//          return false;
//        }
//      }
//    }
//    if (clazz.getSuperclass()!=null) {
//      return isDefaultConstructed(clazz.getSuperclass(), o, defaultConstructed);
//    }
//    return true;
//  }
}

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
package com.heisenberg.impl.util;



/**
 * @author Walter White
 */
public class Exceptions {

  public static void checkNotNull(Object object) {
    checkNotNull(object, "expected a non-null value");
  }

  public static void checkNotNullParameter(Object object, String argumentName) {
    checkNotNull(object, argumentName+" is null");
  }

  public static void checkNotNull(Object object, String description) {
    checkTrue(object!=null, description);
  }

  public static void checkTrue(boolean condition, String description) {
    if (!condition) {
      throw new RuntimeException(description);
    }
  }
}

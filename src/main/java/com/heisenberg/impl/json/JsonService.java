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
package com.heisenberg.impl.json;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;


/**
 * @author Walter White
 */
public interface JsonService {
  
  void registerSubtype(Class<?> subtype);

  String objectToJsonString(Object object);

  String objectToJsonStringPretty(Object object);

  void objectToJson(Object object, Writer writer);
  
  Map<String, Object> objectToJsonMap(Object object);
  
  <T> T jsonToObject(String json, Class<T> type);

  <T> T jsonToObject(Reader reader, Class<T> type);
  
  <T> T jsonMapToObject(Map<String,Object> jsonMap, Class<T> type);
}

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.json;

import java.util.Collection;

import fleetwood.bounder.util.Id;


/**
 * @author Walter White
 */
public interface Serializer {

  void objectStart(Serializable serializable);
  void objectEnd(Serializable serializable);

  void writeStringField(String fieldName, String text);
  void writeTimeField(String fieldName, Long time);
  void writeIdField(String fieldName, Id id);
  void writeNumberField(String fieldDuration, Long end);
  
  void writeObjectArray(String fieldName, Collection<? extends Serializable> serializables);
  void writeObject(String fieldName, Serializable serializable);
  
  void writeString(String serializableType);
}
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

package fleetwood.bounder.type;

import fleetwood.bounder.json.JsonWriter;
import fleetwood.bounder.json.Jsonnable;



/**
 * @author Walter White
 */
public abstract class Type implements Jsonnable {
  
  public static final TextType TEXT = new TextType();
  public static final IdType ID = new IdType();
  
  @Override
  public void write(JsonWriter writer) {
    writer.writeObjectStart(this);
    writer.writeStringField("id", getId());
    writer.writeObjectEnd(this);
  }
  
  public String getId() {
    return getJsonType();
  }

  public void writeValue(JsonWriter writer, Object value) {
    writer.writeString(value.toString());
  }
}

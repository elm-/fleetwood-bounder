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
package com.heisenberg.impl.engine.mongodb;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


/**
 * @author Walter White
 */
public class ObjectIdSerializer extends StdSerializer<ObjectId> {

  public ObjectIdSerializer() {
    super(ObjectId.class);
  }
  
  @Override
  public void serialize(ObjectId value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
    if (value!=null) {
      jgen.writeString(value.toString());
    } else {
      jgen.writeNull();
    }
  }
}

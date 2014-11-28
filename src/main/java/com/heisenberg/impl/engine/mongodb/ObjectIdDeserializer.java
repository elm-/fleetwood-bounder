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
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;


/**
 * @author Walter White
 */
public class ObjectIdDeserializer extends StdDeserializer<ObjectId> {

  private static final long serialVersionUID = 1L;

  static DateTimeFormatter formatter = ISODateTimeFormat.dateTimeParser();

  public ObjectIdDeserializer() {
    super(LocalDateTime.class);
  }

  @Override
  public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    String string = jp.getText();
    if (string!=null) {
      return new ObjectId(string);
    } else {
      return null;
    }
  }
}

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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import fleetwood.bounder.util.Id;


/**
 * @author Walter White
 */
public class JacksonJsonSerializer implements JsonSerializer {
  
  static JsonFactory jsonFactory = new JsonFactory();
  
  protected JsonGenerator json;
  
  public static String toJsonString(JsonSerializable serializable) {
    try {
      StringWriter stringWriter = new StringWriter();
      JsonGenerator json = jsonFactory.createGenerator(stringWriter);
      JacksonJsonSerializer serializer = new JacksonJsonSerializer(json);
      serializable.serialize(serializer);
      serializer.flush();
      stringWriter.flush();
      return stringWriter.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String toJsonStringPretty(JsonSerializable serializable) {
    try {
      StringWriter stringWriter = new StringWriter();
      JsonGenerator json = jsonFactory.createGenerator(stringWriter);
      json.setPrettyPrinter(new DefaultPrettyPrinter());
      JacksonJsonSerializer serializer = new JacksonJsonSerializer(json);
      serializable.serialize(serializer);
      serializer.flush();
      stringWriter.flush();
      return stringWriter.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  void flush() {
    try {
      json.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public JacksonJsonSerializer(JsonGenerator jsonGenerator) {
    this.json = jsonGenerator;
  }

  @Override
  public void objectStart(JsonSerializable serializable) {
    try {
      json.writeStartObject();
      if (serializable instanceof JsonSerializablePolymorphic) {
        JsonSerializablePolymorphic serializablePolymorphic = (JsonSerializablePolymorphic) serializable;
        json.writeFieldName(serializablePolymorphic.getSerializableType());
        json.writeStartObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void objectEnd(JsonSerializable serializable) {
    try {
      json.writeEndObject();
      if (serializable instanceof JsonSerializablePolymorphic) {
        json.writeEndObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeStringField(String fieldName, String text) {
    if (text!=null) {
      try {
        json.writeStringField(fieldName, text);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeTimeField(String fieldName, Long date) {
    if (date!=null) {
      try {
        json.writeNumberField(fieldName, date);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeIdField(String fieldName, Id id) {
    if (id!=null && id.getValue()!=null) {
      try {
        json.writeStringField(fieldName, id.getValue().toString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  @Override
  public void writeNumberField(String fieldName, Long number) {
    if (number!=null) {
      try {
        json.writeNumberField(fieldName, number);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeObjectArray(String fieldName, Collection< ? extends JsonSerializable> serializables) {
    if (serializables!=null) {
      try {
        json.writeFieldName(fieldName);
        json.writeStartArray();
        for (JsonSerializable serializable: serializables) {
          serializable.serialize(this);
        }
        json.writeEndArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeObject(String fieldName, JsonSerializable serializable) {
    if (serializable!=null) {
      try {
        json.writeFieldName(fieldName);
        serializable.serialize(this);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
  }

  @Override
  public void writeString(String string) {
    try {
      if (string!=null) {
        json.writeString(string);
      } else {
        json.writeNull();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

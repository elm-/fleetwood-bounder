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
public class JacksonJsonWriter implements JsonWriter {
  
  static JsonFactory jsonFactory = new JsonFactory();
  
  protected JsonGenerator json;
  
  public static String toJsonString(JsonWritable serializable) {
    try {
      StringWriter stringWriter = new StringWriter();
      JsonGenerator json = jsonFactory.createGenerator(stringWriter);
      JacksonJsonWriter writer = new JacksonJsonWriter(json);
      serializable.write(writer);
      writer.flush();
      stringWriter.flush();
      return stringWriter.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String toJsonStringPretty(JsonWritable serializable) {
    try {
      StringWriter stringWriter = new StringWriter();
      JsonGenerator json = jsonFactory.createGenerator(stringWriter);
      json.setPrettyPrinter(new DefaultPrettyPrinter());
      JacksonJsonWriter writer = new JacksonJsonWriter(json);
      serializable.write(writer);
      writer.flush();
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

  public JacksonJsonWriter(JsonGenerator jsonGenerator) {
    this.json = jsonGenerator;
  }

  @Override
  public void writeId(Id id) {
    try {
      if (id!=null) {
        json.writeString(id.getInternal().toString());
      } else {
        json.writeNull();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void writeIdField(String fieldName, Id id) {
    if (id!=null && id.getInternal()!=null) {
      try {
        json.writeStringField(fieldName, id.getInternal().toString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeNumber(Long l) {
    try {
      if (l!=null) {
        json.writeNumber(l);
      } else {
        json.writeNull();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  @Override
  public void writeNumber(Double d) {
    try {
      if (d!=null) {
        json.writeNumber(d);
      } else {
        json.writeNull();
      }
    } catch (IOException e) {
      e.printStackTrace();
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

  @Override
  public void writeStringField(String fieldName, String string) {
    if (string!=null) {
      try {
        json.writeStringField(fieldName, string);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeTime(Long time) {
    try {
      if (time!=null) {
        json.writeNumber(time);
      } else {
        json.writeNull();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void writeTimeField(String fieldName, Long time) {
    if (time!=null) {
      try {
        json.writeNumberField(fieldName, time);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeObjectStart(JsonWritable serializable) {
    try {
      json.writeStartObject();
      if (serializable instanceof JsonWritablePolymorphic) {
        JsonWritablePolymorphic serializablePolymorphic = (JsonWritablePolymorphic) serializable;
        json.writeFieldName(serializablePolymorphic.getJsonType());
        json.writeStartObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void writeObjectEnd(JsonWritable serializable) {
    try {
      json.writeEndObject();
      if (serializable instanceof JsonWritablePolymorphic) {
        json.writeEndObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void writeObjectArray(String fieldName, Collection< ? extends JsonWritable> serializables) {
    if (serializables!=null) {
      try {
        json.writeFieldName(fieldName);
        json.writeStartArray();
        for (JsonWritable serializable: serializables) {
          serializable.write(this);
        }
        json.writeEndArray();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void writeObject(String fieldName, JsonWritable serializable) {
    if (serializable!=null) {
      try {
        json.writeFieldName(fieldName);
        serializable.write(this);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    
  }

  @Override
  public void writeFieldName(String fieldName) {
    try {
      json.writeFieldName(fieldName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void writeArrayStart() {
    try {
      json.writeStartArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  

  @Override
  public void writeArrayEnd() {
    try {
      json.writeEndArray();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

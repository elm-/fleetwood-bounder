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
package com.heisenberg.instance.json;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.heisenberg.engine.updates.Update;
import com.heisenberg.instance.JsonProcessInstance;
import com.heisenberg.instance.ProcessInstance;
import com.heisenberg.util.Id;
import com.heisenberg.util.Identifyable;


/**
 * @author Walter White
 */
public class Json {
  
  public static ObjectMapper objectMapper;
  public static ObjectWriter objectWriter;
  public static ObjectWriter objectWriterPretty;
  public static ObjectReader objectReader;
  
  static {
    objectMapper = new ObjectMapper()
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
      .setSerializationInclusion(Include.NON_NULL);
//    objectMapper
//      .getDeserializationConfig().getSubtypeResolver()
//        .registerSubtypes(
//                JsonActivityInstanceCreateUpdate.class
//                );
    
    objectWriter = objectMapper.writer();
    objectWriterPretty = objectMapper.writerWithDefaultPrettyPrinter();
    objectReader = objectMapper.reader();
  }


  public static String toJsonString(Jsonnable object) {
    try {
      Object jsonModel = convertToJsonModel(object);
      return objectWriter.writeValueAsString(jsonModel);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static Object convertToJsonModel(Object object) {
    if (object==null) {
      return null;
    } else if (object instanceof ProcessInstance) {
      return new JsonProcessInstance((ProcessInstance)object);
    }
    throw new RuntimeException("Unknown json type: "+object);
  }

  public static String toJsonStringPretty(Object object) {
    try {
      Object jsonModel = convertToJsonModel(object);
      return objectWriterPretty.writeValueAsString(jsonModel);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
  
  public static <T> T fromJsonString(String jsonSource, Class<T> type) {
    try {
      return objectReader.readValue(jsonSource);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getIdString(Identifyable identifyable) {
    if (identifyable==null) return null;
    Id id = identifyable.getId();
    if (id==null) return null;
    return id.toString();
  }
}

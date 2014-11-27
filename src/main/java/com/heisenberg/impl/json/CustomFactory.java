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

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.heisenberg.impl.ProcessEngineImpl;


/**
 * @author Walter White
 */
public class CustomFactory extends HandlerInstantiator {
  
  protected ProcessEngineImpl processEngine;
  protected ActivityTypeIdResolver activityTypeIdResolver;
  protected DataTypeIdResolver dataTypeIdResolver;
  
  public CustomFactory(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
    this.activityTypeIdResolver = new ActivityTypeIdResolver(processEngine);
    this.dataTypeIdResolver = new DataTypeIdResolver(processEngine);
  }

  @Override
  public JsonDeserializer< ? > deserializerInstance(DeserializationConfig config, Annotated annotated, Class< ? > deserClass) {
    return null;
  }

  @Override
  public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class< ? > keyDeserClass) {
    return null;
  }

  @Override
  public JsonSerializer< ? > serializerInstance(SerializationConfig config, Annotated annotated, Class< ? > serClass) {
    return null;
  }

  @Override
  public TypeResolverBuilder< ? > typeResolverBuilderInstance(MapperConfig< ? > config, Annotated annotated, Class< ? > builderClass) {
    return null;
  }

  @Override
  public TypeIdResolver typeIdResolverInstance(MapperConfig< ? > config, Annotated annotated, Class< ? > resolverClass) {
    if (resolverClass==ActivityTypeIdResolver.class) {
      return activityTypeIdResolver;
    } else if (resolverClass==DataTypeIdResolver.class) {
      return dataTypeIdResolver;
    }
    return null;
  }
}

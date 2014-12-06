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
package com.heisenberg.rest;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.jsondeprecated.JsonServiceImpl;


/**
 * @author Walter White
 */
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {
  
  ProcessEngineImpl processEngine;

  public ObjectMapperProvider(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }

  @Override
  public ObjectMapper getContext(Class< ? > type) {
    JsonServiceImpl jsonService = (JsonServiceImpl) processEngine.jsonService;
    return jsonService.objectMapper;
  }
  
  
}

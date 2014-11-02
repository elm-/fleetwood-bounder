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

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import fleetwood.bounder.definition.ActivityDefinitionId;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.definition.TransitionDefinitionId;
import fleetwood.bounder.definition.VariableDefinitionId;
import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstanceId;


/**
 * @author Walter White
 */
public class JacksonJson implements Json {
  
  protected ObjectMapper objectMapper;
  
  public JacksonJson() {
    this.objectMapper = new ObjectMapper()
      .setSerializationInclusion(Include.NON_NULL)
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    
    SimpleModule module = new SimpleModule();
    IdSerializer idSerializer = new IdSerializer();
    module.addSerializer(ProcessInstanceId.class, idSerializer);
    module.addSerializer(ActivityInstanceId.class, idSerializer);
    module.addSerializer(ProcessDefinitionId.class, idSerializer);
    module.addSerializer(ActivityDefinitionId.class, idSerializer);
    module.addSerializer(VariableDefinitionId.class, idSerializer);
    module.addSerializer(TransitionDefinitionId.class, idSerializer);
    this.objectMapper.registerModule(module);
  }
  
  @Override
  public String toJsonString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toJsonStringPretty(Object object) {
    try {
      return objectMapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}

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

import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.instance.ProcessInstance;


/**
 * @author Walter White
 */
public class JacksonJson implements Json {
  
  JsonFactory jsonFactory = new JsonFactory();
  
  public JacksonJson() {
//    this.objectMapper = new ObjectMapper()
//      .setSerializationInclusion(Include.NON_NULL)
//      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
//      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
//    
//    SimpleModule module = new SimpleModule();
//    IdSerializer idSerializer = new IdSerializer();
//    module.addSerializer(ProcessInstanceId.class, idSerializer);
//    module.addSerializer(ActivityInstanceId.class, idSerializer);
//    module.addSerializer(ProcessDefinitionId.class, idSerializer);
//    module.addSerializer(ActivityDefinitionId.class, idSerializer);
//    module.addSerializer(VariableDefinitionId.class, idSerializer);
//    module.addSerializer(TransitionDefinitionId.class, idSerializer);
//    this.objectMapper.registerModule(module);
  }
  
  @Override
  public String toJsonString(Object object) {
    try {
      StringWriter stringWriter = new StringWriter();
      JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
      toJson(object, jsonGenerator);
      jsonGenerator.flush();
      return stringWriter.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toJsonStringPretty(Object object) {
    try {
      StringWriter stringWriter = new StringWriter();
      JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
      jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
      toJson(object, jsonGenerator);
      jsonGenerator.flush();
      return stringWriter.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void toJson(Object object, JsonGenerator jsonGenerator) {
    try {
      if (object==null) {
        jsonGenerator.writeNull();
        return;
      }
      if (object instanceof ProcessInstance) {
        ProcessInstance processInstance = (ProcessInstance)object;
        ProcessInstanceJsonSerializer visitor = new ProcessInstanceJsonSerializer(jsonGenerator);
        visitor.visitProcessInstance(processInstance);
      } else if (object instanceof Collection) {
        jsonGenerator.writeStartArray();
        Collection<?> collection = (Collection<?>) object;
        for (Object element: collection) {
          toJson(element, jsonGenerator);
        }
        jsonGenerator.writeEndArray();
      } else if (object instanceof Update) {
        Update update = (Update)object;
        ProcessInstanceJsonSerializer visitor = new ProcessInstanceJsonSerializer(jsonGenerator);
        visitor.visitUpdate(update);
      } else {
        throw new RuntimeException("No json support for serializing "+object.getClass().getSimpleName()+"'s");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

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
package com.heisenberg.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.heisenberg.api.id.OrganizationId;
import com.heisenberg.api.id.ProcessDefinitionId;
import com.heisenberg.api.id.ProcessId;
import com.heisenberg.api.id.UserId;
import com.heisenberg.definition.PrepareProcessDefinitionForSerialization;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.ValidateProcessDefinitionAfterDeserialization;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.PrepareProcessInstanceForSerialization;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.type.ChoiceType;
import com.heisenberg.type.TextType;
import com.heisenberg.util.Id;


/**
 * @author Walter White
 */
public class Json {
  
  public ProcessEngineImpl processEngine;
  public JsonFactory jsonFactory;
  public ObjectMapper objectMapper;

  public Json(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
    this.jsonFactory = new JsonFactory();
    
    this.objectMapper = new ObjectMapper()
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
      .setSerializationInclusion(Include.NON_NULL);

    this.objectMapper.getDeserializationConfig().getSubtypeResolver().registerSubtypes(
       TextType.class,
       ChoiceType.class
       );
    
    SimpleModule module = new SimpleModule("heisenbergModule", new Version(1, 0, 0, null, null, null));
    module.addSerializer(new IdSerializer());
    module.addSerializer(new IdSerializer());
    module.addDeserializer(ProcessDefinitionId.class, new ProcessDefinitionIdDeserializer());
    module.addDeserializer(ProcessId.class, new ProcessIdDeserializer());
    module.addDeserializer(OrganizationId.class, new OrganizationIdDeserializer());
    module.addDeserializer(UserId.class, new UserIdDeserializer());
    this.objectMapper.registerModule(module);
  }

  public String objectToJsonString(Object object) {
    StringWriter stringWriter = new StringWriter();
    objectToJson(object, stringWriter, objectMapper.writer());
    return stringWriter.toString();
  }

  public String objectToJsonStringPretty(Object object) {
    StringWriter stringWriter = new StringWriter();
    objectToJson(object, stringWriter, objectMapper.writerWithDefaultPrettyPrinter());
    return stringWriter.toString();
  }

  public void objectToJson(Object object, Writer writer) {
    objectToJson(object, writer, objectMapper.writer());
  }

  static final PrepareProcessDefinitionForSerialization PREPARE_PROCESS_DEFINITION_FOR_SERIALIZATION = new PrepareProcessDefinitionForSerialization();
  static final PrepareProcessInstanceForSerialization PREPARE_PROCESS_INSTANCE_FOR_SERIALIZATION = new PrepareProcessInstanceForSerialization();

  protected void objectToJson(Object object, Writer writer, ObjectWriter objectWriter) {
    try {
      if (object instanceof ProcessDefinitionImpl) {
        ((ProcessDefinitionImpl)object).visit(PREPARE_PROCESS_DEFINITION_FOR_SERIALIZATION);
      } else if (object instanceof ProcessInstanceImpl) {
        ((ProcessInstanceImpl)object).visit(PREPARE_PROCESS_INSTANCE_FOR_SERIALIZATION);
      }
      objectWriter
        // .withAttribute("processEngine", processEngine)
        .writeValue(writer, object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public <T> T jsonToObject(String json, Class<T> type) {
    try {
      return jsonToObject(jsonFactory.createParser(json), type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T jsonToObject(Reader reader, Class<T> type) {
    try {
      return jsonToObject(jsonFactory.createParser(reader), type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected <T> T jsonToObject(JsonParser jsonParser, Class<T> type) throws IOException {
    T object = objectMapper
      .reader(type)
      // .withAttribute("processEngine", processEngine)
      .readValue(jsonParser);
    if (type==ProcessDefinitionImpl.class) {
      ValidateProcessDefinitionAfterDeserialization validate = new ValidateProcessDefinitionAfterDeserialization(processEngine);
      ((ProcessDefinitionImpl)object).visit(validate);
      validate.getIssues().checkNoErrors();
    }
    return object;
  }

  private static class IdSerializer extends StdSerializer<Id> {
    protected IdSerializer() {
      super(Id.class);
    }
    @Override
    public void serialize(Id id, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      if (id!=null) {
        jgen.writeString(id.toString());
      } else {
        jgen.writeNull();
      }
    }
  }

  private static class ProcessDefinitionIdDeserializer extends StdDeserializer<ProcessDefinitionId> {
    private static final long serialVersionUID = 1L;
    protected ProcessDefinitionIdDeserializer() {
      super(ProcessDefinitionId.class);
    }
    @Override
    public ProcessDefinitionId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String idText = jp.getText();
      return idText!=null ? new ProcessDefinitionId(idText) : null;
    }
  }

  private static class OrganizationIdDeserializer extends StdDeserializer<OrganizationId> {
    private static final long serialVersionUID = 1L;
    protected OrganizationIdDeserializer() {
      super(OrganizationId.class);
    }
    @Override
    public OrganizationId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String idText = jp.getText();
      return idText!=null ? new OrganizationId(idText) : null;
    }
  }

  private static class UserIdDeserializer extends StdDeserializer<UserId> {
    private static final long serialVersionUID = 1L;
    protected UserIdDeserializer() {
      super(UserId.class);
    }
    @Override
    public UserId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String idText = jp.getText();
      return idText!=null ? new UserId(idText) : null;
    }
  }
  
  private static class ProcessIdDeserializer extends StdDeserializer<ProcessId> {
    private static final long serialVersionUID = 1L;
    protected ProcessIdDeserializer() {
      super(ProcessId.class);
    }
    @Override
    public ProcessId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String idText = jp.getText();
      return idText!=null ? new ProcessId(idText) : null;
    }
  }
}

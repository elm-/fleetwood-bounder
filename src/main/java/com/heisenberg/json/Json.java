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
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.heisenberg.api.id.ActivityInstanceId;
import com.heisenberg.api.id.OrganizationId;
import com.heisenberg.api.id.ProcessDefinitionId;
import com.heisenberg.api.id.ProcessId;
import com.heisenberg.api.id.ProcessInstanceId;
import com.heisenberg.api.id.UserId;
import com.heisenberg.definition.PrepareProcessDefinitionForSerialization;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.ValidateProcessDefinitionAfterDeserialization;
import com.heisenberg.engine.operation.ActivityInstanceStartOperation;
import com.heisenberg.engine.operation.NotifyActivityInstanceEndToParent;
import com.heisenberg.engine.updates.ActivityInstanceCreateUpdate;
import com.heisenberg.engine.updates.ActivityInstanceEndUpdate;
import com.heisenberg.engine.updates.ActivityInstanceStartUpdate;
import com.heisenberg.engine.updates.AsyncOperationAddUpdate;
import com.heisenberg.engine.updates.LockAcquireUpdate;
import com.heisenberg.engine.updates.LockReleaseUpdate;
import com.heisenberg.engine.updates.OperationAddUpdate;
import com.heisenberg.engine.updates.OperationRemoveUpdate;
import com.heisenberg.engine.updates.Update;
import com.heisenberg.form.FormField;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.instance.PrepareProcessInstanceForSerialization;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.type.ChoiceType;
import com.heisenberg.type.TextType;
import com.heisenberg.util.Exceptions;
import com.heisenberg.util.Id;


/**
 * @author Walter White
 */
public class Json {
  
  public ProcessEngineImpl processEngine;
  public JsonFactory jsonFactory;
  public ObjectMapper objectMapper;
  private static final String ATTRIBUTE_KEY_PROCESS_ENGINE = "processEngine";

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
       ChoiceType.class,
       
       ActivityInstanceStartOperation.class,
       NotifyActivityInstanceEndToParent.class,
       
       ActivityInstanceCreateUpdate.class,
       ActivityInstanceEndUpdate.class,
       ActivityInstanceStartUpdate.class,
       AsyncOperationAddUpdate.class,
       LockAcquireUpdate.class,
       LockReleaseUpdate.class,
       OperationAddUpdate.class,
       OperationRemoveUpdate.class
       );
    
    SimpleModule module = new SimpleModule("heisenbergModule", new Version(1, 0, 0, null, null, null));
    module.addSerializer(new IdSerializer());
    module.setSerializerModifier(new BeanSerializerModifier() {
      @SuppressWarnings("unchecked")
      @Override
      public JsonSerializer< ? > modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer< ? > serializer) {
        if (beanDesc.getBeanClass()==FormField.class) {
          return new FormFieldSerializer((JsonSerializer<FormField>) serializer);
        }
        return serializer; 
      }
    });
    module.addDeserializer(ProcessDefinitionId.class, new ProcessDefinitionIdDeserializer());
    module.addDeserializer(ProcessInstanceId.class, new ProcessInstanceIdDeserializer());
    module.addDeserializer(ActivityInstanceId.class, new ActivityInstanceIdDeserializer());
    module.addDeserializer(ProcessId.class, new ProcessIdDeserializer());
    module.addDeserializer(OrganizationId.class, new OrganizationIdDeserializer());
    module.addDeserializer(UserId.class, new UserIdDeserializer());
    this.objectMapper.registerModule(module);
  }
  
  public void registerSubtype(Class<?> subtype) {
    this.objectMapper.registerSubtypes(subtype);
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
      } else if (object instanceof Update) {
        PREPARE_PROCESS_INSTANCE_FOR_SERIALIZATION.update((Update)object, -1);
      }
      objectWriter
        .withAttribute("processEngine", processEngine)
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
      .withAttribute("processEngine", processEngine)
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
  
  private static class ProcessInstanceIdDeserializer extends StdDeserializer<ProcessInstanceId> {
    private static final long serialVersionUID = 1L;
    protected ProcessInstanceIdDeserializer() {
      super(ProcessInstanceId.class);
    }
    @Override
    public ProcessInstanceId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String idText = jp.getText();
      return idText!=null ? new ProcessInstanceId(idText) : null;
    }
  }

  private static class ActivityInstanceIdDeserializer extends StdDeserializer<ActivityInstanceId> {
    private static final long serialVersionUID = 1L;
    protected ActivityInstanceIdDeserializer() {
      super(ActivityInstanceId.class);
    }
    @Override
    public ActivityInstanceId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      String idText = jp.getText();
      return idText!=null ? new ActivityInstanceId(idText) : null;
    }
  }

  private static class FormFieldSerializer extends StdSerializer<FormField> implements ResolvableSerializer {
    JsonSerializer<FormField> defaultSerializer;
    protected FormFieldSerializer(JsonSerializer<FormField> defaultSerializer) {
      super(FormField.class);
      this.defaultSerializer = defaultSerializer;
    }
    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
      ((ResolvableSerializer) defaultSerializer).resolve(provider);
    }
    @Override
    public void serialize(FormField formField, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
      // update json value
      ProcessEngineImpl processEngine = (ProcessEngineImpl) provider.getAttribute(Json.ATTRIBUTE_KEY_PROCESS_ENGINE);
      if (formField!=null) {
        if (formField.value!=null) {
          formField.jsonValue = null;
        }
        if (formField.type==null) {
          Exceptions.checkNotNull(formField.typeId, "No typeId for form field "+formField.id);
          formField.type = processEngine.types.get(formField.typeId);
          Exceptions.checkNotNull(formField.type, "Type "+formField.typeId+" doesn't exist in form field "+formField.id);
        }
        formField.jsonValue = formField.type.convertInternalToJsonValue(formField.value);
      }
      defaultSerializer.serialize(formField, jgen, provider);
    }
  }
}

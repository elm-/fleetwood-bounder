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

import static com.heisenberg.impl.engine.mongodb.MongoReaderHelper.getBoolean;
import static com.heisenberg.impl.engine.mongodb.MongoReaderHelper.getString;
import static com.heisenberg.impl.engine.mongodb.MongoReaderHelper.getTime;
import static com.heisenberg.impl.engine.mongodb.MongoWriterHelper.putOpt;
import static com.heisenberg.impl.engine.mongodb.MongoWriterHelper.putOptId;
import static com.heisenberg.impl.engine.mongodb.MongoWriterHelper.putOptTime;

import java.util.HashMap;
import java.util.Map;

import com.heisenberg.api.util.ActivityInstanceId;
import com.heisenberg.impl.engine.updates.ActivityInstanceCreateUpdate;
import com.heisenberg.impl.engine.updates.ActivityInstanceEndUpdate;
import com.heisenberg.impl.engine.updates.OperationAddNotifyEndUpdate;
import com.heisenberg.impl.engine.updates.OperationAddStartUpdate;
import com.heisenberg.impl.engine.updates.OperationRemoveFirstUpdate;
import com.heisenberg.impl.engine.updates.Update;
import com.heisenberg.impl.json.Json;
import com.mongodb.BasicDBObject;


/**
 * @author Walter White
 */
public class MongoUpdateConverters {

  public static final String TYPE_ACTIVITY_INSTANCE_CREATE = "aic";
  public static final String TYPE_ACTIVITY_INSTANCE_END = "aie";
  public static final String TYPE_OPERATION_ADD_START = "oas";
  public static final String TYPE_OPERATION_ADD_NOTIFY = "oan";
  public static final String TYPE_OPERATION_REMOVE_FIRST = "orf";
  
  public static final String FIELDNAME_TYPE = "t";
  public static final String FIELDNAME_ACTIVITY_INSTANCE_ID = "id";
  public static final String FIELDNAME_ACTIVITY_INSTANCE_NAME = "n";
  public static final String FIELDNAME_ACTIVITY_INSTANCE_START = "s";
  public static final String FIELDNAME_ACTIVITY_INSTANCE_END = "e";
  public static final String FIELDNAME_ACTIVITY_INSTANCE_DURATION = "d";
  public static final String FIELDNAME_ACTIVITY_INSTANCE_OPERATION = "o";
  public static final String FIELDNAME_ASYNC = "a";
  
  Map<Class<? extends Update>, UpdateConverter<? extends Update>> updateConvertersByUpdateClass = new HashMap<>();
  Map<String, UpdateConverter<? extends Update>> updateConvertersByType = new HashMap<>();
  Json json;
  
  public MongoUpdateConverters(Json json) {
    this.json = json;
    registerConverter(new ActivityInstanceCreateConverter(), ActivityInstanceCreateUpdate.class, TYPE_ACTIVITY_INSTANCE_CREATE);
    registerConverter(new ActivityInstanceEndConverter(), ActivityInstanceEndUpdate.class, TYPE_ACTIVITY_INSTANCE_END);
    registerConverter(new OperationAddStartConverter(), OperationAddStartUpdate.class, TYPE_OPERATION_ADD_START);
    registerConverter(new OperationAddNotifyConverter(), OperationAddNotifyEndUpdate.class, TYPE_OPERATION_ADD_NOTIFY);
  }

  protected <T extends Update> void registerConverter(UpdateConverter<T> updateConverter, Class<T> updateClass, String typeId) {
    updateConvertersByUpdateClass.put(updateClass, updateConverter);
    updateConvertersByType.put(typeId, updateConverter);
  }

  public BasicDBObject toDbUpdate(Update update) {
    @SuppressWarnings("unchecked")
    UpdateConverter<Update> updateConverter = (UpdateConverter<Update>) updateConvertersByUpdateClass.get(update.getClass());
    if (updateConverter==null) {
      return null;
    }
    return updateConverter.toDbUpdate(update);
  }

  public Update toUpdate(BasicDBObject dbUpdate) {
    if (dbUpdate==null) {
      return null;
    }
    String typeId = (String) dbUpdate.get(FIELDNAME_TYPE);
    UpdateConverter< ? extends Update> updateConverter = updateConvertersByType.get(typeId);
    return updateConverter.toUpdate(dbUpdate);
  }

  interface UpdateConverter<T extends Update> {
    BasicDBObject toDbUpdate(T update);
    T toUpdate(BasicDBObject dbUpdate);
  }
  
  class ActivityInstanceCreateConverter implements UpdateConverter<ActivityInstanceCreateUpdate> {
    public BasicDBObject toDbUpdate(ActivityInstanceCreateUpdate update) {
      BasicDBObject dbUpdate = new BasicDBObject();
      putOpt(dbUpdate, FIELDNAME_TYPE, TYPE_ACTIVITY_INSTANCE_CREATE);
      putOptId(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_ID, update.activityInstanceId);
      putOptTime(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_START, update.start);
      putOpt(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_NAME, update.activityDefinitionName);
      return dbUpdate;
    }
    public ActivityInstanceCreateUpdate toUpdate(BasicDBObject dbUpdate) {
      ActivityInstanceCreateUpdate update = new ActivityInstanceCreateUpdate();
      update.activityInstanceId = new ActivityInstanceId(dbUpdate.get(FIELDNAME_ACTIVITY_INSTANCE_ID));
      update.start = getTime(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_START);
      update.activityDefinitionName = getString(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_NAME);
      return update;
    }
  }

  class ActivityInstanceEndConverter implements UpdateConverter<ActivityInstanceEndUpdate> {
    public BasicDBObject toDbUpdate(ActivityInstanceEndUpdate update) {
      BasicDBObject dbUpdate = new BasicDBObject();
      putOpt(dbUpdate, FIELDNAME_TYPE, TYPE_ACTIVITY_INSTANCE_END);
      putOptId(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_ID, update.activityInstanceId);
      putOptTime(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_END, update.end);
      return dbUpdate;
    }
    public ActivityInstanceEndUpdate toUpdate(BasicDBObject dbUpdate) {
      ActivityInstanceEndUpdate update = new ActivityInstanceEndUpdate();
      update.activityInstanceId = new ActivityInstanceId(dbUpdate.get(FIELDNAME_ACTIVITY_INSTANCE_ID));
      update.end = getTime(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_END);
      return update;
    }
  }
  
  class OperationAddStartConverter implements UpdateConverter<OperationAddStartUpdate> {
    public BasicDBObject toDbUpdate(OperationAddStartUpdate update) {
      BasicDBObject dbUpdate = new BasicDBObject();
      putOpt(dbUpdate, FIELDNAME_TYPE, TYPE_OPERATION_ADD_START);
      putOptId(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_ID, update.activityInstanceId);
      putOpt(dbUpdate, FIELDNAME_ASYNC, update.isAsync);
      return dbUpdate;
    }
    public OperationAddStartUpdate toUpdate(BasicDBObject dbUpdate) {
      OperationAddStartUpdate update = new OperationAddStartUpdate();
      update.activityInstanceId = new ActivityInstanceId(dbUpdate.get(FIELDNAME_ACTIVITY_INSTANCE_ID));
      update.isAsync = getBoolean(dbUpdate, FIELDNAME_ASYNC);
      return update;
    }
  }

  class OperationAddNotifyConverter implements UpdateConverter<OperationAddNotifyEndUpdate> {
    public BasicDBObject toDbUpdate(OperationAddNotifyEndUpdate update) {
      BasicDBObject dbUpdate = new BasicDBObject();
      putOpt(dbUpdate, FIELDNAME_TYPE, TYPE_OPERATION_ADD_NOTIFY);
      putOptId(dbUpdate, FIELDNAME_ACTIVITY_INSTANCE_ID, update.activityInstanceId);
      putOpt(dbUpdate, FIELDNAME_ASYNC, update.isAsync);
      return dbUpdate;
    }
    public OperationAddNotifyEndUpdate toUpdate(BasicDBObject dbUpdate) {
      OperationAddNotifyEndUpdate update = new OperationAddNotifyEndUpdate();
      update.activityInstanceId = new ActivityInstanceId(dbUpdate.get(FIELDNAME_ACTIVITY_INSTANCE_ID));
      update.isAsync = getBoolean(dbUpdate, FIELDNAME_ASYNC);
      return update;
    }
  }

  class OperationRemoveFirstConverter implements UpdateConverter<OperationRemoveFirstUpdate> {
    public BasicDBObject toDbUpdate(OperationRemoveFirstUpdate update) {
      BasicDBObject dbUpdate = new BasicDBObject();
      putOpt(dbUpdate, FIELDNAME_TYPE, TYPE_OPERATION_REMOVE_FIRST);
      return dbUpdate;
    }
    public OperationRemoveFirstUpdate toUpdate(BasicDBObject dbUpdate) {
      return new OperationRemoveFirstUpdate();
    }
  }
}

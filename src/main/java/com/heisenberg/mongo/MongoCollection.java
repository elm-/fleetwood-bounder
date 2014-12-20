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
package com.heisenberg.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;


/**
 * @author Walter White
 */
public class MongoCollection {
  
  public static final Logger log = MongoWorkflowEngine.log;
  
  protected DBCollection dbCollection;
  protected boolean isPretty; 

  public void writeId(BasicDBObject o, String fieldName, String value) {
    o.put(fieldName, new ObjectId(value));
  }

  public void writeIdOpt(BasicDBObject o, String fieldName, String value) {
    if (value!=null) {
      o.put(fieldName, new ObjectId(value));
    }
  }

  public void writeString(BasicDBObject o, String fieldName, Object value) {
    writeObject(o, fieldName, value);
  }

  public void writeStringOpt(BasicDBObject o, String fieldName, String value) {
    writeObjectOpt(o, fieldName, value);
  }

  public void writeLongOpt(BasicDBObject o, String fieldName, Long value) {
    writeObjectOpt(o, fieldName, value);
  }

  public void writeBooleanOpt(BasicDBObject o, String fieldName, Object value) {
    writeObjectOpt(o, fieldName, value);
  }

  public void writeObject(BasicDBObject o, String fieldName, Object value) {
    o.put(fieldName, value);
  }

  public void writeObjectOpt(BasicDBObject o, String fieldName, Object value) {
    if (value!=null) {
      o.put(fieldName, value);
    }
  }
  
  public void writeTimeOpt(BasicDBObject o, String fieldName, LocalDateTime value) {
    if (value!=null) {
      o.put(fieldName, value.toDate());
    }
  }

  public void writeListElementOpt(BasicDBObject o, String fieldName, Object element) {
    if (element!=null) {
      @SuppressWarnings("unchecked")
      List<Object> list = (List<Object>) o.get(fieldName);
      if (list == null) {
        list = new ArrayList<>();
        o.put(fieldName, list);
      }
      list.add(element);
    }
  }
  
  @SuppressWarnings("unchecked")
  protected List<BasicDBObject> readList(BasicDBObject dbScope, String fieldName) {
    return (List<BasicDBObject>) dbScope.get(fieldName);
  }

  protected Object readObject(BasicDBObject dbObject, String fieldName) {
    return dbObject.get(fieldName);
  }

  protected BasicDBObject readBasicDBObject(BasicDBObject dbObject, String fieldName) {
    return (BasicDBObject) dbObject.get(fieldName);
  }

  protected String readId(BasicDBObject dbObject, String fieldName) {
    Object value = dbObject.get(fieldName);
    return value!=null ? value.toString() : null;
  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> readObjectMap(BasicDBObject dbObject, String fieldName) {
    return (Map<String,Object>) dbObject.get(fieldName);
  }

  protected String readString(BasicDBObject dbObject, String fieldName) {
    return (String) dbObject.get(fieldName);
  }

  protected Long readLong(BasicDBObject dbObject, String fieldName) {
    Object object = dbObject.get(fieldName);
    if (object==null) {
      return null;
    }
    if (object instanceof Long) {
      return (Long) object;
    }
    return ((Number) object).longValue();
  }

  protected Boolean readBoolean(BasicDBObject dbObject, String fieldName) {
    return (Boolean) dbObject.get(fieldName);
  }

  protected LocalDateTime readTime(BasicDBObject dbObject, String fieldName) {
    Date date = (Date) dbObject.get(fieldName);
    return (date!=null ? new LocalDateTime(date) : null);
  }

  protected WriteResult insert(BasicDBObject dbObject, WriteConcern writeConcern) {
    if (log.isDebugEnabled()) log.debug("--"+dbCollection.getName()+"-> insert "+toString(dbObject));
    WriteResult writeResult = dbCollection.insert(dbObject, writeConcern);
    if (log.isDebugEnabled()) log.debug("<-"+dbCollection.getName()+"-- "+writeResult);
    return writeResult;
  }
  
  protected WriteResult save(BasicDBObject dbObject, WriteConcern writeConcern) {
    if (log.isDebugEnabled()) log.debug("--"+dbCollection.getName()+"-> save "+toString(dbObject));
    WriteResult writeResult = dbCollection.save(dbObject, writeConcern);
    if (log.isDebugEnabled()) log.debug("<-"+dbCollection.getName()+"-- "+writeResult);
    return writeResult;
  }
  
  protected WriteResult update(DBObject query, DBObject update, boolean upsert, boolean multi, WriteConcern writeConcern) {
    if (log.isDebugEnabled()) log.debug("--"+dbCollection.getName()+"-> update q="+toString(query)+" u="+toString(update));
    WriteResult writeResult = dbCollection.update(query, update, upsert, multi, writeConcern);
    if (log.isDebugEnabled()) log.debug("<-"+dbCollection.getName()+"-- "+writeResult);
    return writeResult;
  }

  protected BasicDBObject findAndModify(DBObject query, DBObject update) {
    if (log.isDebugEnabled()) log.debug("--"+dbCollection.getName()+"-> findAndModify q="+toString(query)+" u="+toString(update));
    BasicDBObject dbObject = (BasicDBObject) dbCollection.findAndModify( query, null, null, false, update, true, false );
    if (log.isDebugEnabled()) log.debug("<-"+dbCollection.getName()+"-- "+(dbObject!=null ? toString(dbObject) : "null"));
    return dbObject;
  }

  protected BasicDBObject findOne(DBObject query) {
    if (log.isDebugEnabled()) log.debug("--"+dbCollection.getName()+"-> findOne q="+toString(query));
    BasicDBObject dbObject = (BasicDBObject) dbCollection.findOne(query);
    if (log.isDebugEnabled()) log.debug("<-"+dbCollection.getName()+"-- "+toString(dbObject));
    return dbObject;
  }

  protected DBCursor find(DBObject query) {
    return find(query, null);
  }

  protected DBCursor find(DBObject query, DBObject fields) {
    if (log.isDebugEnabled()) log.debug("--"+dbCollection.getName()+"-> find q="+toString(query)+(fields!=null ? " f="+toString(fields) :""));
    return new LoggingCursor(this, dbCollection.find(query, fields));
  }

  protected String toString(Object o) {
    return isPretty ? PrettyPrinter.toJsonPrettyPrint(o) : o.toString();
  }

  protected WriteConcern getWriteConcern(WriteConcern writeConcern) {
    return writeConcern!=null ? writeConcern : dbCollection.getWriteConcern();
  }

  public DBCollection getDbCollection() {
    return dbCollection;
  }

  
  public boolean isPretty() {
    return isPretty;
  }
}

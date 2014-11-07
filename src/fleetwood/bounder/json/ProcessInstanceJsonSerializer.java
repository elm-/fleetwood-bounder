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
import java.util.List;
import java.util.Queue;

import com.fasterxml.jackson.core.JsonGenerator;

import fleetwood.bounder.engine.operation.ActivityInstanceStartOperation;
import fleetwood.bounder.engine.operation.Operation;
import fleetwood.bounder.engine.updates.ActivityInstanceCreateUpdate;
import fleetwood.bounder.engine.updates.ActivityInstanceEndUpdate;
import fleetwood.bounder.engine.updates.ActivityInstanceStartUpdate;
import fleetwood.bounder.engine.updates.LockReleaseUpdate;
import fleetwood.bounder.engine.updates.OperationAddUpdate;
import fleetwood.bounder.engine.updates.OperationRemoveUpdate;
import fleetwood.bounder.engine.updates.ProcessInstanceEndUpdate;
import fleetwood.bounder.engine.updates.Update;
import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.Lock;
import fleetwood.bounder.instance.LockAcquireUpdate;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceVisitor;
import fleetwood.bounder.util.Exceptions;
import fleetwood.bounder.util.Id;


/**
 * @author Walter White
 */
public class ProcessInstanceJsonSerializer extends ProcessInstanceVisitor {

  private static final String ID = "id";
  public static final String OPERATIONS = "operations";
  public static final String OPERATIONTYPE = "type";
  public static final String OPERATIONTYPE_ACTIVITY_INSTANCE_START = "activityInstanceStart";

  public static final String UPDATETYPE = "type";
  public static final String UPDATETYPE_ACTIVITY_INSTANCE_CREATE = "activityInstanceCreate";
  public static final String UPDATETYPE_ACTIVITY_INSTANCE_END = "activityInstanceEnd";
  public static final String UPDATETYPE_ACTIVITY_INSTANCE_START = "activityInstanceStart";
  public static final String UPDATETYPE_LOCK_RELEASE = "lockRelease";
  public static final String UPDATETYPE_LOCK_ACQUIRE = "lockAcquire";
  public static final String UPDATETYPE_OPERATION_ADD = "operationAdd";
  public static final String UPDATETYPE_OPERATION_REMOVE = "operationRemove";
  public static final String UPDATETYPE_PROCESS_INSTANCE_END = "processInstanceEnd";
  
  JsonGenerator jsonGenerator;
  
  public ProcessInstanceJsonSerializer(JsonGenerator jsonGenerator) {
    Exceptions.checkNotNull(jsonGenerator, "jsonGenerator");
    this.jsonGenerator = jsonGenerator;
  }

  @Override
  protected void startProcessInstance(ProcessInstance processInstance) {
    try {
      jsonGenerator.writeStartObject();
      writeIdField(ID, processInstance.getId()); 
      writeTimeField(jsonGenerator, "start", processInstance.getStart()); 
      writeTimeField(jsonGenerator, "end", processInstance.getEnd()); 
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  protected void visitActivityInstances(List<ActivityInstance> activityInstances) {
    try {
      if (activityInstances!=null) {
        jsonGenerator.writeFieldName("activityInstances");
        jsonGenerator.writeStartArray();
        for (ActivityInstance activityInstance: activityInstances) {
          visitActivityInstance(activityInstance);
        }
        jsonGenerator.writeEndArray();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void writeIdField(String fieldName, Id id) throws IOException {
    jsonGenerator.writeStringField(fieldName, id.getValue().toString());
  }

  @Override
  protected void visitUpdates(List<Update> updates) {
    // updates are not serialized to json
    // individual updates or list of updates can be serialized with the JacksonJson class
  }

  @Override
  protected void visitUpdate(Update update) {
    try {
      jsonGenerator.writeStartObject();
      if (ActivityInstanceCreateUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateActivityInstanceCreate((ActivityInstanceCreateUpdate)update);
      } else if (ActivityInstanceEndUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateActivityInstanceEnd((ActivityInstanceEndUpdate)update);
      } else if (ActivityInstanceStartUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateActivityInstanceStart((ActivityInstanceStartUpdate)update);
      } else if (LockAcquireUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateLockAcquire((LockAcquireUpdate)update);
      } else if (LockReleaseUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateLockRelease((LockReleaseUpdate)update);
      } else if (OperationAddUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateOperationAdd((OperationAddUpdate)update);
      } else if (OperationRemoveUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateOperationRemove((OperationRemoveUpdate)update);
      } else if (ProcessInstanceEndUpdate.class.isAssignableFrom(update.getClass())) {
        visitUpdateProcessInstanceEnd((ProcessInstanceEndUpdate)update);
      } else {
        throw new RuntimeException("Unsupported update type: "+update.getClass().getName());
      }
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected void visitUpdateOperationRemove(OperationRemoveUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_OPERATION_REMOVE);
  }

  protected void visitUpdateOperationAdd(OperationAddUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_OPERATION_ADD);
  }

  protected void visitUpdateLockRelease(LockReleaseUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_LOCK_RELEASE);
  }

  protected void visitUpdateLockAcquire(LockAcquireUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_LOCK_ACQUIRE);
    jsonGenerator.writeFieldName("lock");
    visitLock(update.getLock());
  }

  protected void visitUpdateActivityInstanceStart(ActivityInstanceStartUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_ACTIVITY_INSTANCE_START);
    writeIdField("activityInstanceId", update.getActivityInstance().getId());
  }

  protected void visitUpdateActivityInstanceEnd(ActivityInstanceEndUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_ACTIVITY_INSTANCE_END);
    writeIdField("activityInstanceId", update.getActivityInstance().getId());
  }

  protected void visitUpdateProcessInstanceEnd(ProcessInstanceEndUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_PROCESS_INSTANCE_END);
  }

  protected void visitUpdateActivityInstanceCreate(ActivityInstanceCreateUpdate update) throws IOException {
    jsonGenerator.writeStringField(UPDATETYPE, UPDATETYPE_ACTIVITY_INSTANCE_CREATE);
    writeIdField("activityInstanceId", update.getActivityInstance().getId());
  }
  
  protected void visitOperations(Queue<Operation> operations) {
    try {
      if (operations!=null && !operations.isEmpty()) {
        jsonGenerator.writeFieldName(OPERATIONS);
        jsonGenerator.writeStartArray();
        for (Operation operation: operations) {
          visitOperation(operation);
        }
        jsonGenerator.writeEndArray();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void visitOperation(Operation operation) {
    try {
      jsonGenerator.writeStartObject();
      if (ActivityInstanceStartOperation.class.isAssignableFrom(operation.getClass())) {
        visitOperationActivityInstanceStart((ActivityInstanceStartOperation)operation);
      } else {
        throw new RuntimeException("Unsupported operation type: "+operation.getClass().getName());
      }
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected void visitOperationActivityInstanceStart(ActivityInstanceStartOperation operation) throws IOException {
    jsonGenerator.writeStringField(OPERATIONTYPE, OPERATIONTYPE_ACTIVITY_INSTANCE_START);
    jsonGenerator.writeStringField("activityInstanceId", operation.getActivityInstance().getId().toString());
  }

  @Override
  protected void visitLock(Lock lock) {
    try {
      jsonGenerator.writeFieldName("lock");
      jsonGenerator.writeStartObject();
      if (lock.getTime()!=null) {
        writeTimeField(jsonGenerator, "time", lock.getTime());
      }
      if (lock.getOwner()!=null) {
        jsonGenerator.writeStringField("owner", lock.getOwner());
      }
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected void writeTimeField(JsonGenerator jsonGenerator, String fieldName, Long time) throws IOException {
    if (time!=null) {
      jsonGenerator.writeNumberField(fieldName, time);
    }
  }
  
  @Override
  protected void endProcessInstance(ProcessInstance processInstance) {
    try {
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void startActivityInstance(ActivityInstance activityInstance) {
    try {
      jsonGenerator.writeStartObject();
      writeIdField(ID, activityInstance.getId());
      writeTimeField(jsonGenerator, "start", activityInstance.getStart());
      writeTimeField(jsonGenerator, "end", activityInstance.getEnd());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void endActivityInstance(ActivityInstance activityInstance) {
    try {
      jsonGenerator.writeEndObject();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

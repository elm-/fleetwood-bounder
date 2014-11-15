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
package com.heisenberg.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.api.DeployProcessDefinitionResponse;
import com.heisenberg.api.ProcessDefinitionQueryBuilder;
import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.ProcessInstanceQueryBuilder;
import com.heisenberg.api.SignalRequest;
import com.heisenberg.api.StartProcessInstanceRequest;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.definition.ActivityDefinitionImpl;
import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.definition.VariableDefinitionId;
import com.heisenberg.instance.ActivityInstanceId;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.LockImpl;
import com.heisenberg.instance.ProcessInstanceId;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.ActivityTypeDescriptor;
import com.heisenberg.spi.Service;
import com.heisenberg.spi.ServiceDescriptor;
import com.heisenberg.spi.Spi;
import com.heisenberg.spi.Type;
import com.heisenberg.util.Exceptions;
import com.heisenberg.util.Time;

/**
 * @author Walter White
 */
public abstract class ProcessEngineImpl implements ProcessEngine {

  public static final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

  public String id;
  public Map<String,ServiceDescriptor> servicesDescriptors;
  public Map<String,ActivityTypeDescriptor> activityDescriptors;
  public Map<String,Type> types;
  public Executor executor;
  public Json json;
  
  protected void initializeId() {
    try {
      id = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      id = "amnesia";
    }
  }

  protected void initializePluggableImplementations() {
    servicesDescriptors = new HashMap<>();
    activityDescriptors = new HashMap<>();
    types = new HashMap<>();
    Iterator<Spi> spis = ServiceLoader.load(Spi.class).iterator();
    while (spis.hasNext()) {
      Spi spi = spis.next();
      if (spi instanceof Service) {
        Service service = (Service)spi;
        ServiceDescriptor serviceDescriptor = service.getServiceDescriptor();
        serviceDescriptor.serviceClass = service.getClass();
        servicesDescriptors.put(serviceDescriptor.serviceId, serviceDescriptor);
      }
      if (spi instanceof ActivityType) {
        ActivityType activityType = (ActivityType)spi;
        ActivityTypeDescriptor activityTypeDescriptor = activityType.getActivityTypeDescriptor();
        activityTypeDescriptor.activityTypeClass = activityType.getClass();
        activityDescriptors.put(activityTypeDescriptor.activityTypeId, activityTypeDescriptor);
      }
      if (spi instanceof Type) {
        Type type = (Type)spi;
        types.put(type.getId(), type);
      }
    }
  }
  
  protected void initializeExecutor() {
    this.executor = new ScheduledThreadPoolExecutor(4, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  protected void initializeJson() {
    this.json = new Json();
  }

  @Override
  public DeployProcessDefinitionResponse deployProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    DeployProcessDefinitionResponse response = new DeployProcessDefinitionResponse();
    ProcessDefinitionImpl processDefinitionImpl = new ProcessDefinitionImpl(this, response, processDefinition);

    identifyProcessDefinition(processDefinition);
    storeProcessDefinition(processDefinition);
    return processDefinition;
  }
  
  /** ensures that every element in this process definition has an id */
  protected void identifyProcessDefinition(ProcessDefinition processDefinition) {
    processDefinition.id = UUID.randomUUID().toString();
  }
  
  protected abstract void storeProcessDefinition(ProcessDefinition processDefinition);

  public ProcessInstance startProcessInstance(StartProcessInstanceRequest startProcessInstanceRequest) {
    ProcessDefinitionId processDefinitionId = startProcessInstanceRequest.getProcessDefinitionId();
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinitionImpl processDefinition = buildProcessDefinitionQuery()
      .processDefinitionId(processDefinitionId)
      .get();
    ProcessInstanceId processInstanceId = startProcessInstanceRequest.getProcessInstanceId();
    ProcessInstanceImpl processInstance = createProcessInstance(processDefinition, processInstanceId);
    Map<VariableDefinitionId, Object> variableValues = startProcessInstanceRequest.getVariableValues();
    processInstance.setVariableValuesRecursive(variableValues);
    log.debug("Starting "+processInstance);
    processInstance.setStart(Time.now());
    List<ActivityDefinitionImpl> startActivityDefinitions = processDefinition.getStartActivityDefinitions();
    if (startActivityDefinitions!=null) {
      for (ActivityDefinitionImpl startActivityDefinition: startActivityDefinitions) {
        ActivityInstanceImpl activityInstance = processInstance.createActivityInstance(startActivityDefinition);
        processInstance.startActivityInstance(activityInstance);
      }
    }
    LockImpl lock = new LockImpl();
    lock.setTime(Time.now());
    lock.setOwner(getId());
    processInstance.setLock(lock);
    saveProcessInstance(processInstance);
    processInstance.executeOperations();
    return processInstance;
  }

  protected ProcessInstanceImpl createProcessInstance(ProcessDefinitionImpl processDefinition, ProcessInstanceId processInstanceId) {
    if (processInstanceId==null) {
      processInstanceId = createProcessInstanceId(processDefinition);
    }
    ProcessInstanceImpl processInstance = new ProcessInstanceImpl(this, processDefinition, processInstanceId);
    return processInstance;
  }

  protected ProcessInstanceId createProcessInstanceId(ProcessDefinitionImpl processDefinition) {
    return new ProcessInstanceId(UUID.randomUUID());
  }
  
  /** instantiates and assign an id.
   * activityDefinition is only passed for reference.  
   * Apart from choosing the activity instance class to instantiate and assigning the id,
   * this method does not need to link the activity instance to the activityDefinition or parent. */
  public ActivityInstanceImpl createActivityInstance(ActivityDefinitionImpl activityDefinition) {
    ActivityInstanceImpl activityInstance = new ActivityInstanceImpl();
    ActivityInstanceId id = createActivityInstanceId();
    activityInstance.setId(id);
    return activityInstance;
  }

  protected ActivityInstanceId createActivityInstanceId() {
    return new ActivityInstanceId(UUID.randomUUID());
  }

  public ProcessInstanceImpl signal(SignalRequest signalRequest) {
    ActivityInstanceId activityInstanceId = signalRequest.getActivityInstanceId();
    ProcessInstanceImpl processInstance = lockProcessInstanceByActivityInstanceId(activityInstanceId);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(activityInstanceId);
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.signal(activityInstance);
    processInstance.executeOperations();
    return processInstance;
  }
  
  public abstract ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId);

  @Override
  public ProcessDefinitionQueryBuilder buildProcessDefinitionQuery() {
    return new ProcessDefinitionQueryBuilder(this);
  }

  @Override
  public ProcessInstanceQueryBuilder buildProcessInstanceQuery() {
    return new ProcessInstanceQueryBuilder(this);
  }

  public abstract void saveProcessInstance(ProcessInstanceImpl processInstance);

  public abstract void flush(ProcessInstanceImpl processInstance);

  public abstract void flushAndUnlock(ProcessInstanceImpl processInstance);

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public Executor getExecutor() {
    return executor;
  }
  
  public void setExecutor(Executor executor) {
    this.executor = executor;
  }
  
  public Json getJson() {
    return json;
  }
  
  public void setJson(Json json) {
    this.json = json;
  }
}

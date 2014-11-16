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
import com.heisenberg.definition.VariableDefinitionImpl;
import com.heisenberg.expressions.Scripts;
import com.heisenberg.instance.ActivityInstanceId;
import com.heisenberg.instance.ActivityInstanceImpl;
import com.heisenberg.instance.LockImpl;
import com.heisenberg.instance.ProcessInstanceId;
import com.heisenberg.instance.ProcessInstanceImpl;
import com.heisenberg.json.Json;
import com.heisenberg.spi.ActivityParameter;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Service;
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
  public Map<String,ActivityTypeDescriptor> activityTypeDescriptors;
  public Map<String,Type> types;
  public Executor executor;
  public Json json;
  public ProcessDefinitionCache processDefinitionCache;
  public Scripts scripts;
  
  protected ProcessEngineImpl() {
    initialize();
  }

  protected void initialize() {
    initializeId();
    initializeExecutor();
    initializeJson();
    initializeScripts();
    initializePluggableImplementations();
    initializeTypes();
  }

  protected void initializeId() {
    try {
      id = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      id = "amnesia";
    }
  }
  
  protected void initializePluggableImplementations() {
    servicesDescriptors = new HashMap<>();
    activityTypeDescriptors = new HashMap<>();
    types = new HashMap<>();
    Iterator<Spi> spis = ServiceLoader.load(Spi.class).iterator();
    while (spis.hasNext()) {
      Spi spi = spis.next();
      if (spi instanceof Service) {
        Service service = (Service)spi;
        registerService(service);
      }
      if (spi instanceof ActivityType) {
        ActivityType activityType = (ActivityType)spi;
        registerActivityType(activityType);
      }
      if (spi instanceof Type) {
        Type type = (Type)spi;
        registerType(type);
      }
    }
  }

  public ProcessEngineImpl registerType(Type type) {
    types.put(type.getId(), type);
    return this;
  }

  public ProcessEngineImpl registerService(Service service) {
    ServiceDescriptor serviceDescriptor = service.getServiceDescriptor();
    serviceDescriptor.serviceClass = service.getClass();
    servicesDescriptors.put(serviceDescriptor.serviceId, serviceDescriptor);
    return this;
  }

  public ProcessEngineImpl registerActivityType(ActivityType activityType) {
    ActivityTypeDescriptor activityTypeDescriptor = new ActivityTypeDescriptor();
    activityTypeDescriptor.activityTypeId = activityType.getId();
    Exceptions.checkNotNull(activityTypeDescriptor.activityTypeId, "id of "+activityType.getClass().getName()+" can not be null");
    activityTypeDescriptor.activityParameters = ActivityParameter.buildActivityParameterMap(activityType.getActivityParameters());
    activityTypeDescriptor.activityTypeClass = activityType.getClass();
    activityTypeDescriptor.activityType = activityType;
    activityTypeDescriptors.put(activityTypeDescriptor.activityTypeId, activityTypeDescriptor);
    return this;
  }
  
  protected void initializeExecutor() {
    this.executor = new ScheduledThreadPoolExecutor(4, new ThreadPoolExecutor.CallerRunsPolicy());
  }

  protected void initializeJson() {
    this.json = new Json();
  }
  
  protected void initializeScripts() {
    this.scripts = new Scripts();
  }

  protected void initializeTypes() {
    registerType(Type.TEXT);
  }

  @Override
  public DeployProcessDefinitionResponse deployProcessDefinition(ProcessDefinition processDefinition) {
    Exceptions.checkNotNull(processDefinition, "processDefinition");
    DeployProcessDefinitionResponse response = new DeployProcessDefinitionResponse();
    ProcessDefinitionImpl processDefinitionImpl = new ProcessDefinitionImpl(this, response, processDefinition);
    if (!response.hasErrors()) {
      String generatedId = generateProcessDefinitionId(processDefinitionImpl);
      processDefinitionImpl.id = new ProcessDefinitionId(generatedId);
      response.processDefinitionId = generatedId; 
      storeProcessDefinition(processDefinitionImpl);
    }
    return response;
  }
  
  /** ensures that every element in this process definition has an id */
  protected String generateProcessDefinitionId(ProcessDefinitionImpl processDefinition) {
    return UUID.randomUUID().toString();
  }
  
  protected abstract void storeProcessDefinition(ProcessDefinitionImpl processDefinition);

  public ProcessInstance startProcessInstance(StartProcessInstanceRequest startProcessInstanceRequest) {
    ProcessDefinitionId processDefinitionId = new ProcessDefinitionId(startProcessInstanceRequest.processDefinitionRefId);
    Exceptions.checkNotNull(processDefinitionId, "processDefinitionId");
    ProcessDefinitionImpl processDefinition = findProcessDefinitionByIdUsingCache(processDefinitionId);
    ProcessInstanceImpl processInstance = createProcessInstance(processDefinition);
    if (startProcessInstanceRequest.variableValues!=null) {
      Map<String, Object> apiValues = startProcessInstanceRequest.variableValues;
      Map<String, Object> internalValues = new HashMap<>();
      for (String variableDefinitionName: apiValues.keySet()) {
        Object apiValue = apiValues.get(variableDefinitionName);
        VariableDefinitionImpl variableDefinition = processDefinition.findVariableDefinitionByName(variableDefinitionName);
        Object internalValue;
        try {
          internalValue = variableDefinition.type.convertApiToInternalValue(apiValue);
          internalValues.put(variableDefinitionName, internalValue);
        } catch (InvalidApiValueException e) {
          throw new RuntimeException("TODO: change return value that sends the variable validation errors back", e);
        }
      }
      processInstance.setVariableValuesRecursive(internalValues);
    }
      
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
    return processInstance.serializeToJson();
  }

  protected ProcessDefinitionImpl findProcessDefinitionByIdUsingCache(ProcessDefinitionId processDefinitionId) {
    ProcessDefinitionImpl processDefinition = processDefinitionCache!=null ? processDefinitionCache.get(processDefinitionId) : null;
    if (processDefinition==null) {
      processDefinition = loadProcessDefinitionById(processDefinitionId);
    }
    return processDefinition;
  }
  
  protected abstract ProcessDefinitionImpl loadProcessDefinitionById(ProcessDefinitionId processDefinitionId);

  protected ProcessInstanceImpl createProcessInstance(ProcessDefinitionImpl processDefinition) {
    return new ProcessInstanceImpl(this, processDefinition, createProcessInstanceId(processDefinition));
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

  @Override
  public ProcessInstance signal(SignalRequest signalRequest) {
    ActivityInstanceId activityInstanceId = signalRequest.getActivityInstanceId();
    ProcessInstanceImpl processInstance = lockProcessInstanceByActivityInstanceId(activityInstanceId);
    // TODO set variables and context
    ActivityInstanceImpl activityInstance = processInstance.findActivityInstance(activityInstanceId);
    ActivityDefinitionImpl activityDefinition = activityInstance.getActivityDefinition();
    activityDefinition.activityType.signal(activityInstance);
    processInstance.executeOperations();
    throw new RuntimeException("TODO");
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

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
package com.heisenberg.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.definition.ProcessBuilder;
import com.heisenberg.api.definition.ProcessDefinition;
import com.heisenberg.api.id.OrganizationId;
import com.heisenberg.api.id.ProcessDefinitionId;
import com.heisenberg.api.id.ProcessId;
import com.heisenberg.api.id.UserId;
import com.heisenberg.impl.Time;
import com.heisenberg.spi.DataType;


/**
 * @author Walter White
 */
public class ProcessDefinitionImpl extends ScopeDefinitionImpl implements ProcessBuilder, ProcessDefinition {

  /** The globally unique identifier for this process definition. */
  public ProcessDefinitionId id;
  
  /** the types defined on the process definition level.
   * This field is the reference for serialization and storage of types.
   * @link {@link #typesMap} is derived from this field. */
  public List<DataType> dataTypes;

  /** optional time when the process was deployed.
   * This field just serves as a read/write property and is not used during process execution. */
  public LocalDateTime deployedTime;

  /** optional reference to the user that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public UserId deployedBy;

  /** optional reference to the organization (aka tenant or workspace) that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public OrganizationId organizationId;

  /** optional reference to the the source process for which this process definition is one version.
   * This field just serves as a read/write property and is not used during process execution. */
  public ProcessId processId;

  /** optional version number of this process definition, related to @link {@link #processId}.
   * This combined with the @link {@link ScopeDefinitionImpl#name} should be unique. */
  public Long version;

  /** derived from @link {@link #dataTypes} */
  @JsonIgnore
  public Map<String,DataType> typesMap;
  
  /// Process Definition Builder methods /////////////////////////////////////////////

  @Override
  public ProcessDefinitionImpl name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public ProcessDefinitionImpl deployedTime(LocalDateTime deployedAt) {
    this.deployedTime = deployedAt;
    return this;
  }

  @Override
  public ProcessDefinitionImpl deployedUserId(UserId deployedBy) {
    this.deployedBy = deployedBy;
    return this;
  }

  @Override
  public ProcessDefinitionImpl processId(ProcessId processId) {
    this.processId = processId;
    return this;
  }

  @Override
  public ProcessDefinitionImpl version(Long version) {
    this.version = version;
    return this;
  }
  
  @Override
  public ProcessDefinitionImpl organizationId(OrganizationId organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  @Override
  public ProcessDefinitionImpl line(Long line) {
    super.line(line);
    return this;
  }

  @Override
  public ProcessDefinitionImpl column(Long column) {
    super.column(column);
    return this;
  }
  
  @Override
  public ActivityDefinitionImpl newActivity() {
    return super.newActivity();
  }

  @Override
  public VariableDefinitionImpl newVariable() {
    return super.newVariable();
  }

  @Override
  public TransitionDefinitionImpl newTransition() {
    return super.newTransition();
  }

  @Override
  public TimerDefinitionImpl newTimer() {
    return super.newTimer();
  }
  
  @Override
  public ProcessDefinitionImpl dataType(DataType dataType) {
    if (dataTypes==null) {
      dataTypes = new ArrayList<DataType>();
    }
    dataTypes.add(dataType);
    return this;
  }
  
  // other methods ////////////////////////////////////////////////////////////////////
  
  public void prepare() {
    this.processDefinition = this;
    if (dataTypes!=null) {
      typesMap = new HashMap<>();
      for (DataType dataType: dataTypes) {
        typesMap.put(dataType.getId(), dataType);
      }
    }
    super.prepare();
  }
  
  public ProcessDefinitionPath getPath() {
    return new ProcessDefinitionPath();
  }

  public ProcessDefinitionId getId() {
    return id;
  }
  
  public void setId(ProcessDefinitionId id) {
    this.id = id;
  }
  
  public String toString() {
    return id!=null ? id.toString() : Integer.toString(System.identityHashCode(this));
  }

  public DataType findDataType(String dataTypeId) {
    DataType dataType = typesMap!=null ? typesMap.get(dataTypeId) : null;
    if (dataType!=null) {
      return dataType;
    }
    return processEngine.findDataType(dataTypeId);
  }

  public void visit(ProcessDefinitionVisitor visitor) {
    if (visitor==null) {
      return;
    }
    // If some visitor needs to control the order of types vs other content visited, 
    // then this is the idea you should consider 
    //   if (visitor instanceof OrderedProcessDefinitionVisitor) {
    //     ... also delegate the ordering of this visit to the visitor ... 
    //   } else { ... perform the default as below
    visitor.startProcessDefinition(this);
    visitTypes(visitor);
    super.visit(visitor);
    visitor.endProcessDefinition(this);
  }

  public void visitTypes(ProcessDefinitionVisitor visitor) {
    if (dataTypes!=null) {
      for (int i=0; i<dataTypes.size(); i++) {
        DataType dataType = dataTypes.get(i);
        visitor.dataType(dataType, i);
      }
    }
  }
}

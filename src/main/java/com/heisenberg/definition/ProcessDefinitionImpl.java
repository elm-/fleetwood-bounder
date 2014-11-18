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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.definition.ProcessBuilder;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class ProcessDefinitionImpl extends ScopeDefinitionImpl implements ProcessBuilder {

  /** The globally unique identifier for this process definition. */
  public ProcessDefinitionId id;
  
  /** the types defined on the process definition level.
   * This field is the reference for serialization and storage of types.
   * @link {@link #typesMap} is derived from this field. */
  public List<Type> types;

  /** optional time when the process was deployed.
   * This field just serves as a read/write property and is not used during process execution. */
  public Long deployedAt;

  /** optional reference to the user that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public UserId deployedBy;

  /** optional reference to the organization (aka tenant or workspace) that deployed the process definition.
   * This field just serves as a read/write property and is not used during process execution. */
  public OrganizationId organizationRefId;

  /** optional reference to the the source process for which this process definition is one version.
   * This field just serves as a read/write property and is not used during process execution. */
  public ProcessId processRefId;

  /** optional version number of this process definition, related to @link {@link #processRefId}.
   * This combined with the @link {@link ScopeDefinitionImpl#name} should be unique. */
  public Long version;

  /** derived from @link {@link #types} */
  @JsonIgnore
  public Map<String,Type> typesMap;
  
  /// Process Definition Builder methods /////////////////////////////////////////////

  @Override
  public ProcessDefinitionImpl name(String name) {
    super.name(name);
    return this;
  }

  @Override
  public ProcessDefinitionImpl deployedTime(Long deployedAt) {
    this.deployedAt = deployedAt;
    return this;
  }

  @Override
  public ProcessDefinitionImpl deployedUserId(UserId deployedBy) {
    this.deployedBy = deployedBy;
    return this;
  }

  @Override
  public ProcessDefinitionImpl processId(ProcessId processId) {
    this.processRefId = processId;
    return this;
  }

  @Override
  public ProcessDefinitionImpl version(Long version) {
    this.version = version;
    return this;
  }
  
  @Override
  public ProcessDefinitionImpl organizationId(OrganizationId organizationId) {
    this.organizationRefId = organizationId;
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
  public ProcessDefinitionImpl type(Type type) {
    if (types==null) {
      types = new ArrayList<Type>();
    }
    types.add(type);
    return this;
  }
  
  public void parse(ParseContext parseContext) {
    if (types!=null) {
      typesMap = new HashMap<>();
      for (int i=0; i<types.size(); i++) {
        Type type = types.get(i);
        parseContext.pushPathElement(type, type.getId(), i);
        type.parse(parseContext);
        parseContext.popPathElement();
        typesMap.put(type.getId(), type);
      }
    }
    super.parse(parseContext);
  }
  
  // other methods ////////////////////////////////////////////////////////////////////
  
  public void prepare() {
    this.processDefinition = this;
    if (types!=null) {
      typesMap = new HashMap<>();
      for (Type type: types) {
        typesMap.put(type.getId(), type);
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

  public void visit(ProcessDefinitionVisitor visitor) {
    if (visitor==null) {
      return;
    }
    visitor.startProcessDefinition(this);
    super.visit(visitor);
    visitor.endProcessDefinition(this);
  }

  public Type findType(String typeId) {
    Type type = typesMap!=null ? typesMap.get(typeId) : null;
    if (type!=null) {
      return type;
    }
    return processEngine.types.get(typeId);
  }
}

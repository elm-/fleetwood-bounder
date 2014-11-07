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

package fleetwood.bounder.definition;

import fleetwood.bounder.instance.ProcessEngineImpl;
import fleetwood.bounder.util.Identifyable;


/**
 * @author Walter White
 */
public class VariableDefinition implements Identifyable {

  protected ProcessEngineImpl processEngine;
  protected ProcessDefinition processDefinition;  
  protected CompositeDefinition parent;
  protected VariableDefinitionId id;
  protected String name;
  
  public void prepare() {
  }

  public ProcessEngineImpl getProcessStore() {
    return processEngine;
  }

  public void setProcessStore(ProcessEngineImpl processEngine) {
    this.processEngine = processEngine;
  }
  
  public CompositeDefinition getParent() {
    return parent;
  }

  public void setParent(CompositeDefinition parent) {
    this.parent = parent;
  }

  public VariableDefinitionId getId() {
    return id;
  }

  public void setId(VariableDefinitionId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }
  
  public VariableDefinition setName(String name) {
    this.name = name;
    return this;
  }
  
  public ProcessDefinition getProcessDefinition() {
    return processDefinition;
  }
  
  public void setProcessDefinition(ProcessDefinition processDefinition) {
    this.processDefinition = processDefinition;
  }
}

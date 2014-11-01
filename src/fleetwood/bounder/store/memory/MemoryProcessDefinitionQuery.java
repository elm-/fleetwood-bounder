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

package fleetwood.bounder.store.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fleetwood.bounder.definition.ProcessDefinition;
import fleetwood.bounder.definition.ProcessDefinitionId;
import fleetwood.bounder.store.ProcessDefinitionQuery;


/**
 * @author Tom Baeyens
 */
public class MemoryProcessDefinitionQuery extends ProcessDefinitionQuery {
  
  MemoryProcessStore memoryProcessStore;
  
  public MemoryProcessDefinitionQuery(MemoryProcessStore memoryProcessStore) {
    super(memoryProcessStore);
    this.memoryProcessStore = memoryProcessStore;
  }

  @Override
  public ProcessDefinition get() {
    Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = memoryProcessStore.processDefinitions;
    if (processDefinitionId!=null) {
      return processDefinitions.get(processDefinitionId);
    }
    if (processDefinitions.isEmpty()) {
      return null;
    }
    for (ProcessDefinition processDefinition: processDefinitions.values()) {
      if (satisfiesCriteria(processDefinition)) {
        return processDefinition;
      }
    }
    return null;
  }

  @Override
  public List<ProcessDefinition> asList() {
    Map<ProcessDefinitionId, ProcessDefinition> processDefinitions = memoryProcessStore.processDefinitions;
    List<ProcessDefinition> result = new ArrayList<>();
    for (ProcessDefinition processDefinition: processDefinitions.values()) {
      if (satisfiesCriteria(processDefinition)) {
        result.add(processDefinition);
      }
    }
    return result;
  }

  boolean satisfiesCriteria(ProcessDefinition processDefinition) {
    if ( processDefinitionId!=null
         && !processDefinitionId.equals(processDefinition.getId()) ) {
      return false;
    }
    return true;
  }
}

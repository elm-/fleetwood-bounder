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
package com.heisenberg.engine.mongodb;

import java.util.List;

import com.heisenberg.api.id.ActivityInstanceId;
import com.heisenberg.api.id.ProcessDefinitionId;
import com.heisenberg.definition.ProcessDefinitionImpl;
import com.heisenberg.impl.ProcessDefinitionQuery;
import com.heisenberg.impl.ProcessEngineImpl;
import com.heisenberg.impl.ProcessInstanceQuery;
import com.heisenberg.instance.ProcessInstanceImpl;



/**
 * @author Walter White
 */
public class MongoDbProcessEngine extends ProcessEngineImpl {

  @Override
  protected void storeProcessDefinition(ProcessDefinitionImpl processDefinition) {
  }

  @Override
  protected ProcessDefinitionImpl loadProcessDefinitionById(ProcessDefinitionId processDefinitionId) {
    return null;
  }

  @Override
  public ProcessInstanceImpl lockProcessInstanceByActivityInstanceId(ActivityInstanceId activityInstanceId) {
    return null;
  }

  @Override
  public void saveProcessInstance(ProcessInstanceImpl processInstance) {
  }

  @Override
  public void flush(ProcessInstanceImpl processInstance) {
  }

  @Override
  public void flushAndUnlock(ProcessInstanceImpl processInstance) {
  }

  @Override
  public List<ProcessInstanceImpl> findProcessInstances(ProcessInstanceQuery processInstanceQuery) {
    return null;
  }

  @Override
  public List<ProcessDefinitionImpl> findProcessDefinitions(ProcessDefinitionQuery processDefinitionQuery) {
    return null;
  }
}

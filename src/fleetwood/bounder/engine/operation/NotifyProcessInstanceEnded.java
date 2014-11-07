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

package fleetwood.bounder.engine.operation;

import fleetwood.bounder.instance.ProcessEngineImpl;
import fleetwood.bounder.instance.ProcessInstance;


/**
 * @author Walter White
 */
public class NotifyProcessInstanceEnded implements Operation {

  protected ProcessInstance processInstance;

  public NotifyProcessInstanceEnded(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  @Override
  public void execute(ProcessEngineImpl processEngine) {
    // TODO notify other process instances waiting for this one to complete.
  }

  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }
  
  @Override
  public boolean isAsync() {
    return false;
  }
}

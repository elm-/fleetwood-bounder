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

package fleetwood.bounder;

import java.util.List;

import fleetwood.bounder.instance.ActivityInstanceId;
import fleetwood.bounder.instance.ProcessInstance;
import fleetwood.bounder.instance.ProcessInstanceId;


/**
 * @author Walter White
 */
public class ProcessInstanceQueryBuilder {

  protected ProcessEngine processEngine;
  protected ProcessInstanceQuery processInstanceQuery;

  public ProcessInstanceQueryBuilder(ProcessEngine processEngine) {
    super();
    this.processEngine = processEngine;
    this.processInstanceQuery = new ProcessInstanceQuery();
  }
  
  public ProcessInstanceQueryBuilder processInstanceId(ProcessInstanceId id) {
    processInstanceQuery.setProcessInstanceId(id);
    return this;
  }
  
  public ProcessInstanceQueryBuilder activityInstanceId(ActivityInstanceId id) {
    processInstanceQuery.setActivityInstanceId(id);
    return this;
  }
  
  public ProcessInstance get() {
    processInstanceQuery.setMaxResults(1);
    List<ProcessInstance> processInstances = asList();
    if (processInstances!=null && !processInstances.isEmpty()) {
      return processInstances.get(0);
    }
    return null;
  }

  public List<ProcessInstance> asList() {
    return processEngine.findProcessInstances(processInstanceQuery);
  }

  public ProcessInstanceQuery getQuery() {
    return processInstanceQuery;
  }
}

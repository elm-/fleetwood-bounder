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


/**
 * @author Tom Baeyens
 */
public class TransitionDefinition {

  protected TransitionDefinitionId id;
  protected ActivityDefinition from;
  protected ActivityDefinition to;

  public TransitionDefinitionId getId() {
    return id;
  }
  
  public void setId(TransitionDefinitionId id) {
    this.id = id;
  }

  
  public ActivityDefinition getFrom() {
    return from;
  }

  
  public void setFrom(ActivityDefinition from) {
    this.from = from;
  }

  
  public ActivityDefinition getTo() {
    return to;
  }

  
  public void setTo(ActivityDefinition to) {
    this.to = to;
  }
}

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

import fleetwood.bounder.instance.ActivityInstance;
import fleetwood.bounder.instance.CompositeInstance;
import fleetwood.bounder.util.Exceptions;


/**
 * @author Walter White
 */
public abstract class ActivityDefinition extends CompositeDefinition {

  protected ActivityDefinitionId id;

  public ActivityDefinitionId getId() {
    return id;
  }
  
  public void setId(ActivityDefinitionId id) {
    this.id = id;
  }
  
  public abstract void start(ActivityInstance activityInstance);

  public ProcessDefinitionPath getPath() {
    Exceptions.checkNotNull(id, "Activity definition doesn't have an id yet");
    Exceptions.checkNotNull(parent, "Activity definition doesn't have an parent yet");
    return parent.getPath().addActivityInstanceId(id);
  }

  public String toString() {
    return id!=null ? "["+id.toString()+"]" : "["+Integer.toString(System.identityHashCode(this))+"]";
  }

  public void signal(ActivityInstance activityInstance) {
    activityInstance.onwards();
  }

//  public void executionPathEnded(ActivityInstance nestedActivityInstance) {
//    CompositeInstance compositeInstance = nestedActivityInstance.getParent();
//    if (!compositeInstance.hasUnfinishedActivityInstances()) {
//      compositeInstance.end();
//    }
//  }

  @Override
  public void visit(ProcessDefinitionVisitor visitor) {
    visitor.startActivityDefinition(this);
    super.visit(visitor);
    visitor.endActivityDefinition(this);
  }
}

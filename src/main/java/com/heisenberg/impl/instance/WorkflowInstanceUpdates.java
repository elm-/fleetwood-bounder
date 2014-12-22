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
package com.heisenberg.impl.instance;


/**
 * @author Walter White
 */
public class WorkflowInstanceUpdates extends ScopeInstanceUpdates {

  public boolean isLockChanged;
  public boolean isWorkChanged;
  public boolean isAsyncWorkChanged;

  public WorkflowInstanceUpdates(boolean isNew) {
    this.isNew = isNew;
  }

  public void reset(boolean isNew) {
    super.reset(isNew);
    isLockChanged = false;
    isWorkChanged = false;
    isAsyncWorkChanged = false;
  }
}

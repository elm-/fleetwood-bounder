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
package com.heisenberg.api.activitytypes;

import com.heisenberg.impl.plugin.Binding;


/**
 * @author Walter White
 */
public class CallMapping {
  
  protected Binding<Object> sourceBinding;
  protected String destinationVariableId;

  public CallMapping sourceBinding(Binding<Object> sourceBinding) {
    this.sourceBinding = sourceBinding;
    return this;
  }

  public CallMapping destinationVariableId(String destinationVariableId) {
    this.destinationVariableId = destinationVariableId;
    return this;
  }
}

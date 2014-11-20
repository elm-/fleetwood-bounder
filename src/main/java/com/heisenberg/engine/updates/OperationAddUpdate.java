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
package com.heisenberg.engine.updates;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.engine.operation.Operation;


/** The operation is always added at the end of the queue. 
 * 
 * @author Walter White
 */
@JsonTypeName("operationAdd")
public class OperationAddUpdate extends OperationUpdate {

  public OperationAddUpdate() {
    super();
  }

  public OperationAddUpdate(Operation operation) {
    super(operation);
  }
  
}

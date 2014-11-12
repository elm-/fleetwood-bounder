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

package fleetwood.bounder.engine.updates;

import fleetwood.bounder.engine.operation.Operation;
import fleetwood.bounder.json.JsonReader;
import fleetwood.bounder.json.JsonTypeId;
import fleetwood.bounder.json.JsonWriter;



/** always the first operation in the queue is removed.
 * 
 * @author Walter White
 */
@JsonTypeId("operationRemove")
public class OperationRemoveUpdate extends OperationUpdate {

  public OperationRemoveUpdate(Operation operation) {
    super(operation);
  }

  @Override
  public void write(JsonWriter writer) {
    writer.writeObjectStart(this);
    writer.writeObjectEnd(this);
  }

  @Override
  public void read(JsonReader reader) {
  }
}

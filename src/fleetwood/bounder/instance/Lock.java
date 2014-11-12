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

package fleetwood.bounder.instance;

import fleetwood.bounder.json.JsonReader;
import fleetwood.bounder.json.JsonWriter;
import fleetwood.bounder.json.Jsonnable;


/**
 * @author Walter White
 */
public class Lock implements Jsonnable {

  public static String FIELD_TIME = "time";
  protected Long time;

  public static String FIELD_OWNER = "owner";
  protected String owner;
  
  public Long getTime() {
    return time;
  }
  
  public void setTime(Long time) {
    this.time = time;
  }
  
  public String getOwner() {
    return owner;
  }
  
  public void setOwner(String owner) {
    this.owner = owner;
  }

  @Override
  public void write(JsonWriter writer) {
    writer.writeObjectStart(this);
    writer.writeTimeField(FIELD_TIME, time);
    writer.writeStringField(FIELD_OWNER, owner);
    writer.writeObjectEnd(this);
  }

  @Override
  public void read(JsonReader reader) {
  }
}

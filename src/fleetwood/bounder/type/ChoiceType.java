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

package fleetwood.bounder.type;

import java.util.ArrayList;
import java.util.List;

import fleetwood.bounder.json.JsonReader;
import fleetwood.bounder.json.JsonTypeId;
import fleetwood.bounder.json.JsonWriter;


/**
 * @author Walter White
 */
@JsonTypeId("choice")
public class ChoiceType extends Type {
  
  protected List<ChoiceOption> options;

  public ChoiceType option(String label) {
    option(label, null);
    return this;
  }

  public ChoiceType option(String label, String id) {
    if (options==null) {
      options = new ArrayList<>();
    }
    options.add(new ChoiceOption()
      .id(id)
      .label(label)
    );
    return this;
  }

  public List<ChoiceOption> getOptions() {
    return options;
  }

  public void setOptions(List<ChoiceOption> options) {
    this.options = options;
  }

  @Override
  public void write(JsonWriter writer) {
    writer.writeObjectStart(this);
    writer.writeStringField("id", getId());
    writer.writeObjectArray("options", options);
    writer.writeObjectEnd(this);
  }
  
  public void writeValue(JsonWriter writer, Object value) {
    writer.writeString((String)value);
  }

  @Override
  public void read(JsonReader reader) {
  }
}

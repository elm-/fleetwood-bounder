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
package com.heisenberg.api.type;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Walter White
 */
public class ChoiceDescriptor extends TypeDescriptor {

  public List<ChoiceOption> options;
  
  @Override
  public ChoiceDescriptor id(String id) {
    super.id(id);
    return this;
  }

  public ChoiceDescriptor option(String optionId, String optionLabel) {
    if (options==null) {
      options = new ArrayList<>();
    }
    options.add(new ChoiceOption()
      .id(optionId)
      .label(optionLabel));
    return this;
  }
}

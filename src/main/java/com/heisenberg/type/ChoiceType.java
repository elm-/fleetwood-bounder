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
package com.heisenberg.type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.heisenberg.api.type.ChoiceOption;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Type;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public class ChoiceType extends Type {
  
  public static final String ID = "choice";
  
  protected String id;
  /** maps option ids to option labels */
  protected Map<String, String> optionsMap;
  
  public ChoiceType(String id, List<ChoiceOption> options) {
    Exceptions.checkNotNullParameter(id, "id");
    Exceptions.checkNotNullParameter(options, "options");
    this.id = id;
    this.optionsMap = new HashMap<>();
    for (ChoiceOption option: options) {
      optionsMap.put(option.id, option.label);
    }
  }

//  public ChoiceType option(String label) {
//    option(label, null);
//    return this;
//  }
//
//  public ChoiceType option(String label, String id) {
//    if (options==null) {
//      options = new ArrayList<>();
//    }
//    options.add(new ChoiceOption()
//      .id(id)
//      .label(label)
//    );
//    return this;
//  }
//
//  public List<ChoiceOption> getOptions() {
//    return options;
//  }
//
//  public void setOptions(List<ChoiceOption> options) {
//    this.options = options;
//  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Object convertApiToInternalValue(Object apiValue) throws InvalidApiValueException {
    if ( apiValue!=null 
         && !optionsMap.containsKey(apiValue)) {
      throw new InvalidApiValueException("Invalid value '"+apiValue+"'.  Expected one of "+optionsMap.keySet()+" (or null)");
    }
    return apiValue; 
  }
}

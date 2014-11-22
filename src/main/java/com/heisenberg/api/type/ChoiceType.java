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

import java.util.HashMap;
import java.util.Map;

import com.heisenberg.spi.AbstractDataType;
import com.heisenberg.spi.InvalidApiValueException;


/**
 * @author Walter White
 */
public class ChoiceType extends AbstractDataType {
  
  protected String id;
  protected String label;
  /** maps option ids to option labels */
  protected Map<String, String> options;
  
  public ChoiceType option(String id, String label) {
    if (options==null) {
      options = new HashMap<>();
    }
    options.put(id, label);
    return this;
  }
  
  public ChoiceType id(String id) {
    this.id = id;
    return this;
  }

  public ChoiceType label(String label) {
    this.label = label;
    return this;
  }

  @Override
  public String getId() {
    return id;
  }
  
  public Map<String,String> getOptions() {
    return options;
  }

  @Override
  public Object convertJsonToInternalValue(Object apiValue) throws InvalidApiValueException {
    if ( apiValue!=null 
         && !options.containsKey(apiValue) ) {
      throw new InvalidApiValueException("Invalid value '"+apiValue+"'.  Expected one of "+options.keySet()+" (or null)");
    }
    return apiValue; 
  }

  @Override
  public String getLabel() {
    return label;
  }
}
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

import com.heisenberg.definition.ProcessDefinitionId;
import com.heisenberg.spi.InvalidApiValueException;
import com.heisenberg.spi.Type;


/**
 * @author Walter White
 */
public class ProcessDefinitionIdType extends Type {

  @Override
  public String getId() {
    return "processDefinitionId";
  }

  @Override
  public Object convertApiToInternalValue(Object apiValue) throws InvalidApiValueException {
    if (apiValue==null) return null;
    if (apiValue instanceof ProcessDefinitionIdType) return apiValue;
    if (apiValue instanceof String) return new ProcessDefinitionId(apiValue);
    throw new InvalidApiValueException("Invalid process definition id: "+apiValue);
  }

}

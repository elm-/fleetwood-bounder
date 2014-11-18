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
package com.heisenberg.form;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Walter White
 */
public class Form {
  
  public List<FormField> fields;

  public void addField(FormField field) {
    if (fields==null) {
      fields = new ArrayList<>();
    }
    fields.add(field);
  }

  public Object getFieldValue(String fieldId) {
    FormField field = getField(fieldId);
    return field!=null ? field.value : null;
  }

  public FormField getField(String fieldId) {
    if (fieldId==null) return null;
    if (fields!=null) {
      for (FormField field: fields) {
        if (fieldId.equals(field.id)) {
          return field;
        }
      }
    }
    return null;
  }

  public void parseValues(Form configurationForm) {
    
  }
}

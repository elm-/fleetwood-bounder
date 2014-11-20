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
package com.heisenberg.spi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.heisenberg.form.Form;
import com.heisenberg.form.FormField;
import com.heisenberg.type.ObjectField;
import com.heisenberg.util.Exceptions;
import com.heisenberg.util.Reflection;



/**
 * @author Walter White
 */
public class SpiDescriptor {

  public SpiType spiType;
  public String typeName;
  public String label;
  // TODO protected String description;
  // TODO protected byte[] icon;
  // TODO protected String iconMimeType;

  @JsonIgnore
  public Class<?> spiClass;
  public List<ObjectField> configurationFields;

  public SpiDescriptor(Spi spiObject) {
    this.spiClass = spiObject.getClass();
    initializeSpiType();
    initializeConfigurationForm();
    initializeTypeName(); 
  }

  protected void initializeSpiType() {
    if (Type.class.isAssignableFrom(spiClass)) {
      this.spiType = SpiType.type;
    } else if (ActivityType.class.isAssignableFrom(spiClass)) {
      this.spiType = SpiType.activity;
    } else {
      throw new RuntimeException(spiClass.getName()+" doesn't implement "+Type.class.getName()+" nor "+ActivityType.class.getName());
    }
  }
  
  protected void initializeConfigurationForm() {
    List<Field> fields =  Reflection.getFieldsRecursive(spiClass, Reflection.NOT_STATIC);
    if (!fields.isEmpty()) {
      configurationFields = new ArrayList<ObjectField>(fields.size());
      for (Field field : fields) {
        configurationFields.add(new ObjectField(field));
      }
    }
  }
  
  protected void initializeTypeName() {
    this.typeName = spiClass.getName();
  }

  public Class< ? > getSpiClass() {
    return spiClass;
  }

  public List<ObjectField> getConfigurationFields() {
    return configurationFields;
  }

  public SpiType getSpiType() {
    return spiType;
  }
  
  public String getTypeName() {
    return typeName;
  }
  
  public String getLabel() {
    return label;
  }
}

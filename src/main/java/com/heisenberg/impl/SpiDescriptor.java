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
package com.heisenberg.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.ConfigurationField;
import com.heisenberg.spi.DataType;
import com.heisenberg.spi.Spi;
import com.heisenberg.util.Reflection;



/**
 * @author Walter White
 */
public class SpiDescriptor {

  @JsonIgnore
  public ProcessEngineImpl processEngine;
  
  public SpiType spiType;
  public String typeName;
  public String label;
  // TODO protected String description;
  // TODO protected byte[] icon;
  // TODO protected String iconMimeType;

  @JsonIgnore
  public Class<?> spiClass;
  public List<SpiDescriptorField> configurationFields;

  public SpiDescriptor(ProcessEngineImpl processEngine, Spi spiObject) {
    this.processEngine = processEngine;
    this.spiClass = spiObject.getClass();
    initializeSpiType();
    initializeConfigurationFields();
    initializeTypeName(); 
  }

  protected void initializeSpiType() {
    if (DataType.class.isAssignableFrom(spiClass)) {
      this.spiType = SpiType.type;
    } else if (ActivityType.class.isAssignableFrom(spiClass)) {
      this.spiType = SpiType.activity;
    } else {
      throw new RuntimeException(spiClass.getName()+" doesn't implement "+DataType.class.getName()+" nor "+ActivityType.class.getName());
    }
  }
  
  protected void initializeConfigurationFields() {
    List<Field> fields =  Reflection.getFieldsRecursive(spiClass);
    if (!fields.isEmpty()) {
      configurationFields = new ArrayList<SpiDescriptorField>(fields.size());
      for (Field field : fields) {
        if (field.getAnnotation(ConfigurationField.class)!=null) {
          configurationFields.add(new SpiDescriptorField(processEngine, field));
        }
      }
    }
  }
  
  protected void initializeTypeName() {
    this.typeName = spiClass.getName();
  }

  public Class< ? > getSpiClass() {
    return spiClass;
  }

  public List<SpiDescriptorField> getConfigurationFields() {
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

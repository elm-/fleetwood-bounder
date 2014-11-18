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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.form.Form;
import com.heisenberg.type.ObjectField;
import com.heisenberg.type.ObjectType;
import com.heisenberg.util.Exceptions;


/**
 * @author Walter White
 */
public class SpiDescriptor extends ObjectType {

  protected SpiType spiType;
  protected String label;
  // TODO protected String description;
  // TODO protected byte[] icon;
  // TODO protected String iconMimeType;
  
  @JsonIgnore
  protected Class<?> spiClass;

  public SpiDescriptor(Class<?> spiClass) {
    this(spiClass, instantiate(spiClass));
  }

  public SpiDescriptor(Object spiObject) {
    this(spiObject.getClass(), spiObject);
  }

  public SpiDescriptor(Class<?> spiClass, Object spiObject) {
    super(spiClass);
    this.spiClass = spiClass;
    this.id = spiObject.getClass().getName();
    if (spiObject instanceof Type) {
      this.spiType = SpiType.type;
    } else if (spiObject instanceof ActivityType) {
      this.spiType = SpiType.activity;
    } else {
      throw new RuntimeException(id+" doesn't implement "+Type.class.getName()+" nor "+ActivityType.class.getName());
    }
  }

  static Object instantiate(Class< ? > spiClass) {
    Exceptions.checkNotNullParameter(spiClass, "spiClass");
    try {
      return spiClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Couldn't instantiate "+spiClass+" with the default constructor: "+e.getMessage(), e);
    }
  }
  
  public Object instantiateAndConfigure(Form configurationInProcessExport) {
    if (spiClass==null) {
      try {
        spiClass = Class.forName(id);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Couldn't load class for instantiating and configuring an spi "+id, e);
      }
    }
    Object spiObject = instantiate(spiClass);
    if (fields!=null) {
      for (ObjectField objectField: fields) {
        try {
          Object formFieldValue = configurationInProcessExport.getFieldValue(objectField.name);
          Field field = spiClass.getDeclaredField(objectField.name);
          field.setAccessible(true);
          field.set(spiObject, formFieldValue);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
    return spiObject;
  }

  public SpiType getSpiType() {
    return spiType;
  }
  
  public String getLabel() {
    return label;
  }
}

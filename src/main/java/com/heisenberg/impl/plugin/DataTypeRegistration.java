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
package com.heisenberg.impl.plugin;

import com.heisenberg.impl.AbstractProcessEngine;
import com.heisenberg.impl.type.DataType;


/**
 * @author Walter White
 */
public abstract class DataTypeRegistration {

  public abstract void register(AbstractProcessEngine processEngine, DataTypeService dataTypeService);

  public static class JavaBean extends DataTypeRegistration {
    Class<?> javaBeanClass;
    public JavaBean() {
    }
    public JavaBean(Class< ? > javaBeanClass) {
      this.javaBeanClass = javaBeanClass;
    }
    @Override
    public void register(AbstractProcessEngine processEngine, DataTypeService dataTypeService) {
      dataTypeService.registerJavaBeanType(javaBeanClass);
    }
    public Class< ? > getJavaBeanClass() {
      return javaBeanClass;
    }
    public void setJavaBeanClass(Class< ? > javaBeanClass) {
      this.javaBeanClass = javaBeanClass;
    }
  }

  public static class Singleton extends DataTypeRegistration {
    DataType dataType;
    String typeId;
    Class<?> valueClass;
    public Singleton() {
    }
    public Singleton(DataType dataType, String typeId, Class< ? > valueClass) {
      this.dataType = dataType;
      this.typeId = typeId;
      this.valueClass = valueClass;
    }
    @Override
    public void register(AbstractProcessEngine processEngine, DataTypeService dataTypeService) {
      dataTypeService.registerSingletonDataType(dataType, typeId, valueClass);
    }
    public DataType getDataType() {
      return dataType;
    }
    public void setDataType(DataType dataType) {
      this.dataType = dataType;
    }
    public String getTypeId() {
      return typeId;
    }
    public void setTypeId(String typeId) {
      this.typeId = typeId;
    }
    public Class< ? > getValueClass() {
      return valueClass;
    }
    public void setValueClass(Class< ? > valueClass) {
      this.valueClass = valueClass;
    }
  }

  public static class Configurable extends DataTypeRegistration {
    DataType dataType;
    public Configurable() {
    }
    public Configurable(DataType dataType) {
      this.dataType = dataType;
    }
    @Override
    public void register(AbstractProcessEngine processEngine, DataTypeService dataTypeService) {
      dataTypeService.registerConfigurableDataType(dataType);
    }
    public DataType getDataType() {
      return dataType;
    }
    public void setDataType(DataType dataType) {
      this.dataType = dataType;
    }
  }
}

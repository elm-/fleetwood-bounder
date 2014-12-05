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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heisenberg.api.type.DataType;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public class DataTypeDescriptor extends TypeDescriptor {

  protected DataType dataType; 
  
  public DataTypeDescriptor(DataType dataType) {
    Exceptions.checkNotNullParameter(dataType, "dataType");
    this.dataType = dataType;
  }

  public DataTypeDescriptor(Class<? extends DataType> dataTypeClass, DataTypes dataTypes) {
    super(dataTypeClass);
    Exceptions.checkNotNullParameter(dataTypeClass, "dataTypeClass");
    Exceptions.checkNotNullParameter(dataTypes, "dataTypes");
    this.dataTypeClass = dataTypeClass;
    this.configurationFields = dataTypes.initializeConfigurationFields(dataTypeClass);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

  
  public String getType() {
    return id;
  }

  
  public void setTypeId(String dataTypeId) {
    this.id = dataTypeId;
  }

  
  public DataType getDataType() {
    return dataType;
  }

  
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  
  public Class< ? extends DataType> getDataTypeClass() {
    return dataTypeClass;
  }

  
  public void setDataTypeClass(Class< ? extends DataType> dataTypeClass) {
    this.dataTypeClass = dataTypeClass;
  }

  
  public List<TypeField> getConfigurationFields() {
    return configurationFields;
  }

  
  public void setConfigurationFields(List<TypeField> configurationFields) {
    this.configurationFields = configurationFields;
  }
}

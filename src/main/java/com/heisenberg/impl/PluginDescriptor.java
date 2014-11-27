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
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.util.Plugin;
import com.heisenberg.impl.util.Reflection;



/**
 * @author Walter White
 */
public class PluginDescriptor {

  @JsonIgnore
  protected ProcessEngineImpl processEngine;

  @JsonIgnore
  protected Class<?> pluginClass;
  protected String typeId;
  protected String label;
  protected List<PluginConfigurationField> configurationFields;

  public PluginDescriptor(ProcessEngineImpl processEngine, Plugin pluginObject) {
    this.processEngine = processEngine;
    this.pluginClass = pluginObject.getClass();
    this.typeId = pluginObject.getTypeId();
    initializeConfigurationFields();
  }

  protected void initializeConfigurationFields() {
    List<Field> fields =  Reflection.getFieldsRecursive(pluginClass);
    if (!fields.isEmpty()) {
      configurationFields = new ArrayList<PluginConfigurationField>(fields.size());
      for (Field field : fields) {
        ConfigurationField configurationField = field.getAnnotation(ConfigurationField.class);
        if (field.getAnnotation(ConfigurationField.class)!=null) {
          PluginConfigurationField descriptorField = new PluginConfigurationField(processEngine, field, configurationField);
          configurationFields.add(descriptorField);
        }
      }
    }
  }
  
  public Class< ? > getPluginClass() {
    return pluginClass;
  }

  public List<PluginConfigurationField> getConfigurationFields() {
    return configurationFields;
  }

  public String getTypeId() {
    return typeId;
  }
  
  public String getLabel() {
    return label;
  }
}

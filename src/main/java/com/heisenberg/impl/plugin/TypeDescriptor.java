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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.api.activities.ConfigurationField;
import com.heisenberg.api.util.Plugin;
import com.heisenberg.impl.type.DataType;
import com.heisenberg.impl.util.Exceptions;
import com.heisenberg.impl.util.Reflection;


/**
 * @author Walter White
 */
public class TypeDescriptor {

  protected ActivityType activityType; 
  protected DataType dataType; 
  
  protected String label;
  protected String description;
// protected byte[] iconBytes;
// protected String iconMimeType;
// datatypes might also need a javascript rendering configuration
  protected List<TypeField> configurationFields;
  
  public TypeDescriptor() {
  }

  public TypeDescriptor(ActivityType activityType) {
    this.activityType = activityType;
  }

  public TypeDescriptor(DataType dataType) {
    this.dataType = dataType;
  }

  public List<TypeField> analyze(ActivityTypes activityTypes) {
    return analyze(activityTypes.dataTypes);
  }

  public List<TypeField> analyze(DataTypes dataTypes) {
    Class<?> pluginClass = (activityType!=null ? activityType.getClass() : dataType.getClass());
    List<Field> fields = Reflection.getNonStaticFieldsRecursive(pluginClass);
    if (fields.isEmpty()) {
      return null;
    }
    configurationFields = new ArrayList<TypeField>(fields.size());
    for (Field field : fields) {
      ConfigurationField configurationField = field.getAnnotation(ConfigurationField.class);
      if (field.getAnnotation(ConfigurationField.class) != null) {
        TypeDescriptor typeDescriptor = dataTypes.getTypeDescriptor(field);
        TypeField typeField = new TypeField(field, typeDescriptor.dataType, configurationField);
        configurationFields.add(typeField);
      }
    }
    return configurationFields;
  }

  public TypeDescriptor(Plugin plugin) {
    Exceptions.checkNotNull(plugin);
  }
}

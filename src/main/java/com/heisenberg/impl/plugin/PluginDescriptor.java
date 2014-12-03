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

import com.heisenberg.api.util.Plugin;
import com.heisenberg.impl.util.Exceptions;
import com.heisenberg.impl.util.Reflection;


/**
 * @author Walter White
 */
public class PluginDescriptor {

  protected String type;
  protected String label;
  protected String description;
//  protected byte[] iconBytes;
//  protected String iconMimeType;
  
  public PluginDescriptor() {
  }

  public PluginDescriptor(Class<? extends Plugin> pluginClass) {
    this(Reflection.newInstance(pluginClass));
  }
  
  public PluginDescriptor(Plugin plugin) {
    Exceptions.checkNotNull(plugin);
    this.type = plugin.getType();
    this.label = plugin.getLabel();
    this.description = plugin.getDescription();
  }
}

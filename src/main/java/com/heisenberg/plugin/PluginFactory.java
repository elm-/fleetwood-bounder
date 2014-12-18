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
package com.heisenberg.plugin;

import com.heisenberg.api.ProcessEngineConfiguration;
import com.heisenberg.impl.ProcessEngineImpl;


/** super interface for all service provider interfaces.
 * 
 * By using this marker interface, 
 * we only have to do 1 scan to find all pluggable implementations.
 * @see ProcessEngineImpl#initializeDefaultPluggableImplementations()  
 * 
 * @author Walter White
 */
public interface PluginFactory {

  void registerPlugins(ProcessEngineConfiguration processEngineConfiguration);
}

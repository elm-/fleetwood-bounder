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
package com.heisenberg.api;

import com.heisenberg.api.builder.ActivityInstanceQuery;
import com.heisenberg.api.builder.MessageBuilder;
import com.heisenberg.api.builder.ProcessDefinitionBuilder;
import com.heisenberg.api.builder.StartBuilder;
import com.heisenberg.api.plugin.ActivityTypes;
import com.heisenberg.api.plugin.DataSources;
import com.heisenberg.api.plugin.DataTypes;
import com.heisenberg.api.plugin.ProcessProfileBuilder;
import com.heisenberg.api.plugin.Triggers;


/** Start here.
 * 
 * Obtain a process engine by instantiating one of the concrete classes like this:
 * 
 * <pre>{@code
 * ProcessEngine processEngine = new MemoryProcessEngine();
 * }</pre>
 * 
 * @author Walter White
 */
public interface ProcessEngine {
  
  /** Start building a new process, see  */
  ProcessDefinitionBuilder newProcessDefinition();

  /** Use a {@link StartBuilder trigger} to start a new process instance for a process definition. */
  StartBuilder newStart();

  /** Use a {@link MessageBuilder message} to end a waiting activity instance in a process instance. */
  MessageBuilder newMessage();

  ActivityInstanceQuery newActivityInstanceQuery();
  
  ProcessDefinitionQuery newProcessDefinitionQuery();
  
  /** sends the process engine configuration (ie the activity types, data sources, data types 
   * and triggers) to the process editor.  Once this is done, the process builder will allow you 
   * to select a profile when creating a new process. 
   * @return A textual description of the result.  Can be 'Profile {profileName} was up to date', 'Profile {profileName} created', 'Profile {profileName} updated' or a communication error */
  ProcessProfileBuilder newProcessProfile();
  
  ActivityTypes getActivityTypes();
  DataTypes getDataTypes();
  DataSources getDataSources();
  Triggers getTriggers();
}

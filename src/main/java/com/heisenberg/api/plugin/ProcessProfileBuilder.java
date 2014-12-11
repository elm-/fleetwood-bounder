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
package com.heisenberg.api.plugin;

import com.heisenberg.impl.ProcessProfileImpl;


/**
 * @author Walter White
 */
public interface ProcessProfileBuilder {

  ProcessProfileImpl profileKey(String profileKey);

  ProcessProfileImpl profileName(String profileName);
  
  ProcessProfileImpl authenticationToken(String authenticationToken);
  
  ProcessProfileImpl authentication(String username, String password);

  ProcessProfileImpl scheme(String scheme);
  
  ProcessProfileImpl server(String server);
  
  ProcessProfileImpl port(Integer port);
  
  ProcessProfileImpl organizationKey(String organizationKey);
  
  String update();
}

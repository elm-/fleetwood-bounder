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
package com.heisenberg.client;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.ProcessEngineConfiguration;


/**
 * @author Walter White
 */
public class ClientProcessEngineConfiguration extends ProcessEngineConfiguration {

  public String baseUrl;

  public ProcessEngine buildProcessEngine() {
    return new ClientProcessEngine(this);
  }

  public ClientProcessEngineConfiguration baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public String getBaseUrl() {
    return baseUrl!=null ? baseUrl : createDefaultBaseUrl();
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public static String createDefaultBaseUrl() {
    return "http://localhost:9999";
  }
}

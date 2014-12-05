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

import com.heisenberg.api.configuration.JsonService;
import com.heisenberg.api.configuration.ProcessEngineConfiguration;
import com.heisenberg.impl.client.ClientProcessEngine;


/**
 * @author Walter White
 */
public class ClientProcessEngineConfiguration {

  public JsonService jsonService;
  public String baseUrl;

  public ProcessEngine buildProcessEngine() {
    return new ClientProcessEngine(this);
  }

  public ClientProcessEngineConfiguration jsonService(JsonService jsonService) {
    this.jsonService = jsonService;
    return this;
  }

  public ClientProcessEngineConfiguration baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public JsonService getJsonService() {
    return jsonService!=null ? jsonService : createDefaultJsonService();
  }
  
  public void setJsonService(JsonService jsonService) {
    this.jsonService = jsonService;
  }
  
  public String getBaseUrl() {
    return baseUrl!=null ? baseUrl : createDefaultBaseUrl();
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public static JsonService createDefaultJsonService() {
    return ProcessEngineConfiguration.createDefaultJsonService();
  }

  public static String createDefaultBaseUrl() {
    return "http://localhost:9999";
  }
}

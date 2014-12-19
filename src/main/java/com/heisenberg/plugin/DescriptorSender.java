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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heisenberg.impl.json.JsonService;
import com.heisenberg.impl.util.Exceptions;


/**
 * @author Walter White
 */
public class DescriptorSender {
  
  public static final Logger log = LoggerFactory.getLogger(DescriptorSender.class);

  protected Descriptors descriptors;
  protected JsonService jsonService;
  protected String authenticationToken;
  protected String authenticationUsername;
  protected String authenticationPassword;
  protected String scheme = "https";
  protected String server = "api.effektif.com";
  protected Integer port;
  protected String organizationKey;

  public DescriptorSender() {
  }

  public DescriptorSender(Descriptors descriptors, JsonService jsonService) {
    this.descriptors = descriptors;
    this.jsonService = jsonService;
  }

  public String send() {
    // TODO add HTTP
    log.debug("Sending process profile over HTTP to the process builder:");
    log.debug(">>> PUT "+getUrl());
    // TODO send over HTTP to the server
    log.debug(">>> "+jsonService.objectToJsonStringPretty(descriptors));
    return "the http response, which is hopefully 200 OK";
  }
  
  public DescriptorSender authenticationToken(String authenticationToken) {
    this.authenticationToken = authenticationToken;
    return this;
  }
  
  public DescriptorSender authentication(String username, String password) {
    this.authenticationUsername = username;
    this.authenticationPassword = password;
    return this;
  }

  public DescriptorSender scheme(String scheme) {
    this.scheme = scheme;
    return this;
  }
  
  public DescriptorSender server(String server) {
    this.server = server;
    return this;
  }
  
  public DescriptorSender port(Integer port) {
    this.port = port;
    return this;
  }
  
  public DescriptorSender organizationKey(String organizationKey) {
    this.organizationKey = organizationKey;
    return this;
  }
  
  public String getAuthenticationToken() {
    return authenticationToken;
  }
  
  public void setAuthenticationToken(String authenticationToken) {
    this.authenticationToken = authenticationToken;
  }
  
  public String getAuthenticationUsername() {
    return authenticationUsername;
  }
  
  public void setAuthenticationUsername(String authenticationUsername) {
    this.authenticationUsername = authenticationUsername;
  }
  
  public String getAuthenticationPassword() {
    return authenticationPassword;
  }
  
  public void setAuthenticationPassword(String authenticationPassword) {
    this.authenticationPassword = authenticationPassword;
  }

  
  public String getScheme() {
    return scheme;
  }

  
  public void setScheme(String scheme) {
    this.scheme = scheme;
  }

  
  public String getServer() {
    return server;
  }

  
  public void setServer(String server) {
    this.server = server;
  }

  
  public Integer getPort() {
    return port;
  }

  
  public void setPort(Integer port) {
    this.port = port;
  }

  public String getUrl() {
    Exceptions.checkNotNull(scheme, "profileName is null");
    Exceptions.checkNotNull(server, "server is null");
    Exceptions.checkNotNull(organizationKey, "organizationKey is null");
    return scheme+"://"+server+(port!=null ? ":"+port : "")+"/api/v1/"+organizationKey+"/profile";
  }
}

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

import java.util.ArrayList;
import java.util.List;

import com.heisenberg.api.util.Page;


/**
 * @author Walter White
 */
public class PageImpl<T> implements Page<T> {
  
  List<T> results = new ArrayList<T>();

  public PageImpl(ActivityInstanceQueryImpl activityInstanceQuery) {
  }

  @Override
  public List<T> getResults() {
    return results;
  }

  public void add(T result) {
    if (result!=null) {
      results.add(result);
    }
  }
}

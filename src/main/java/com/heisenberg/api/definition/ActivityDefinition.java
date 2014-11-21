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
package com.heisenberg.api.definition;

import java.util.List;

import com.heisenberg.spi.ActivityType;
import com.heisenberg.spi.Validator;



/**
 * @author Walter White
 */
public interface ActivityDefinition {
  
  ActivityDefinition getActivityDefinition(String name);

  List<ActivityDefinition> getActivityDefinitions();

  String getName();

  void initializeStartActivities(Validator validator);

  /** If you use this, you must call the @link {@link #initializeStartActivities()} in the @link {@link ActivityType#validate(ActivityDefinition, com.heisenberg.spi.Validator)} */
  List<ActivityDefinition> getStartActivities();
}

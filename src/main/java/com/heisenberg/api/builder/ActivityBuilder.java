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
package com.heisenberg.api.builder;

import com.heisenberg.api.activities.ActivityType;
import com.heisenberg.impl.type.DataType;



/**
 * @author Walter White
 */
public interface ActivityBuilder {

  ActivityBuilder activityType(ActivityType activityType);

  ActivityBuilder id(String activityDefinitionId);

  ActivityBuilder newActivity();

  TransitionBuilder newTransition();

  VariableBuilder newVariable();

  TimerBuilder newTimer();

  ActivityBuilder line(Long lineNumber);
  
  ActivityBuilder column(Long columnNumber);
  
  ActivityBuilder defaultTransition(String transitionId);

  ActivityBuilder forEach(String elementVariableId, DataType elementDataType, String collectionVariableId);
  
  ActivityBuilder forEachExpression(String elementVariableId, DataType elementDataType, String collectionExpression); 
}

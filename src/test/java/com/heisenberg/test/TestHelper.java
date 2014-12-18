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
package com.heisenberg.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import com.heisenberg.api.ProcessEngine;
import com.heisenberg.api.instance.ActivityInstance;
import com.heisenberg.api.instance.ProcessInstance;
import com.heisenberg.api.instance.ScopeInstance;
import com.heisenberg.mongo.MongoProcessEngine;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

/**
 * @author Walter White
 */
public class TestHelper {
  
  public static void assertTextPresent(String expected, String actual) {
    if (actual==null || !actual.contains(expected)) {
      Assert.fail("Expected "+expected+" but was "+actual);
    }
  }

  public static ActivityInstance findActivityInstanceOpen(ProcessInstance processInstance, Object activityDefinitionId) {
    return findActivityInstanceOpen(processInstance.getActivityInstances(), activityDefinitionId); 
  }

  static ActivityInstance findActivityInstanceOpen(List<? extends ActivityInstance> activityInstances, Object activityDefinitionId) {
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance: activityInstances) {
        ActivityInstance theOne = findActivityInstanceOpen(activityInstance, activityDefinitionId);
        if (theOne!=null) {
          return theOne;
        }
      }
    }
    return null;
  }
  
  static ActivityInstance findActivityInstanceOpen(ActivityInstance activityInstance, Object activityDefinitionId) {
    if (activityDefinitionId.equals(activityInstance.getActivityDefinitionId())) {
      return activityInstance;
    }
    return findActivityInstanceOpen(activityInstance.getActivityInstances(), activityDefinitionId);
  }

  public static void assertOpen(ProcessInstance processInstance, String... expectedActivityNames) {
    Map<String,Integer> expectedActivityCounts = new HashMap<String, Integer>();
    if (expectedActivityNames!=null) {
      for (String expectedActivityName: expectedActivityNames) {
        Integer count = expectedActivityCounts.get(expectedActivityName);
        expectedActivityCounts.put(expectedActivityName, count!=null ? count+1 : 1);
      }
    }
    Map<String,Integer> activityCounts = new HashMap<String, Integer>();
    scanActivityCounts(processInstance, activityCounts);
    assertEquals(expectedActivityCounts, activityCounts);
  }
  
  static void scanActivityCounts(ScopeInstance scopeInstance, Map<String, Integer> activityCounts) {
    List< ? extends ActivityInstance> activityInstances = scopeInstance.getActivityInstances();
    if (activityInstances!=null) {
      for (ActivityInstance activityInstance : activityInstances) {
        if (!activityInstance.isEnded()) {
          Object activityId = activityInstance.getActivityDefinitionId();
          Integer count = activityCounts.get(activityId);
          activityCounts.put(activityId.toString(), count != null ? count + 1 : 1);
          scanActivityCounts(activityInstance, activityCounts);
        }
      }
    }
  }

  public static String getActivityInstanceId(ProcessInstance processInstance, String activityDefinitionId) {
    ActivityInstance activityInstance = processInstance.findActivityInstanceByActivityDefinitionId(activityDefinitionId);
    Assert.assertNotNull("No open activity instance found "+activityDefinitionId+" not found", activityInstance);
    return activityInstance.getId();
  }

  public static ProcessInstance endTask(ProcessEngine processEngine, ProcessInstance processInstance, String activityDefinitionId) {
    return processEngine.newMessage()
      .activityInstanceId(getActivityInstanceId(processInstance, activityDefinitionId))
      .send();
  }
  
  public static void mongoDeleteAllCollections(ProcessEngine processEngine) {
    MongoProcessEngine mongoProcessEngine = (MongoProcessEngine) processEngine;
    mongoDeleteAllDocumentsInCollection(mongoProcessEngine.getProcessDefinitions().getDbCollection());
    mongoDeleteAllDocumentsInCollection(mongoProcessEngine.getProcessInstances().getDbCollection());
    mongoDeleteAllDocumentsInCollection(mongoProcessEngine.getJobService().getJobs().getDbCollection());
  }

  public static void mongoDeleteAllDocumentsInCollection(DBCollection dbCollection) {
    DBCursor documents = dbCollection.find();
    while (documents.hasNext()) {
      dbCollection.remove(documents.next());
    }
  }
}

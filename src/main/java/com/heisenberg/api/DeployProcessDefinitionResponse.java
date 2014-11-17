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

import java.util.List;
import java.util.Locale;


/**
 * @author Walter White
 */
public class DeployProcessDefinitionResponse {

  public String processDefinitionId;
  public List<Issue> issues;
  
  /** throws a RuntimeException if there were errors deploying the process */
  public DeployProcessDefinitionResponse checkNoErrors() {
    checkNoIssues(false);
    return this;
  }

  /** throws a RuntimeException if there were errors or warnings while deploying the process */
  public DeployProcessDefinitionResponse checkNoErrorsAndNoWarnings() {
    checkNoIssues(true);
    return this;
  }

  void checkNoIssues(boolean throwIfWarning) {
    if (issues!=null) {
      for (Issue issue: issues) {
        if (issue.type==Issue.IssueType.error || throwIfWarning) {
          throw new RuntimeException(getIssueReport());
        }
      }
    }
  }
  
  public boolean hasIssues() {
    return issues!=null && !issues.isEmpty();
  }

  public String getIssueReport() {
    return getIssueReport(null);
  }

  public String getIssueReport(Locale l) {
    if (hasIssues()) {
      StringBuilder issueReport = new StringBuilder();
      issueReport.append("Issues: \n");
      for (Issue issue: issues) {
        issueReport.append(issue.toString());
        issueReport.append("\n");
      }
      return issueReport.toString();
    }
    return null;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public boolean hasErrors() {
    if (hasIssues()) {
      for (Issue issue: issues) {
        if (Issue.IssueType.error==issue.type) {
          return true;
        }
      }
    }
    return false;
  }
}

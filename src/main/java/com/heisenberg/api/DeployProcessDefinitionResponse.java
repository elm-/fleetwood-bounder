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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.heisenberg.api.definition.Location;


/**
 * @author Walter White
 */
public class DeployProcessDefinitionResponse {

  public String processDefinitionId;
  public List<Issue> issues;
  
  public class Issue {
    IssueType type;
    Location location;
    String msg; 
    Object[] msgArgs; // msg and arguments are split so that msg can be translated first.
    public String getFormattedMessage() {
      return getFormattedMessage(null);
    }
    public String getFormattedMessage(Locale l) {
      return String.format(l, msg, msgArgs);
    }
  }
  
  public enum IssueType {
    error,
    warning
  }
  
  public void addError(Location location, String msg, Object... msgArgs) {
    addIssue(IssueType.error, location, msg, msgArgs);
  }
  
  public void addWarning(Location location, String msg, Object... msgArgs) {
    addIssue(IssueType.warning, location, msg, msgArgs);
  }
  
  void addIssue(IssueType type, Location location, String msg, Object... msgArgs) {
    if (issues==null) {
      issues = new ArrayList<>();
    }
    Issue issue = new Issue();
    issue.type = type;
    issue.location = location;
    issue.msg = msg;
    issue.msgArgs = msgArgs;
    issues.add(issue);
  }
  
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
        if (issue.type==IssueType.error || throwIfWarning) {
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
        if (IssueType.error==issue.type) {
          issueReport.append("| ERROR   | ");
        } else {
          issueReport.append("| warning | ");
        }
        issueReport.append(issue.getFormattedMessage(l));
        issueReport.append(" | ");
        issueReport.append(issue.location);
        issueReport.append("|\n");
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
        if (IssueType.error==issue.type) {
          return true;
        }
      }
    }
    return false;
  }
}

/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.definition;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinition extends Composite {

  protected ProcessDefinitionId id;

  public ProcessDefinitionId getId() {
    return id;
  }
  
  public void setId(ProcessDefinitionId id) {
    this.id = id;
  }
}

/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.definition;


/**
 * @author Tom Baeyens
 */
public class Variable {

  protected VariableId id;
  protected String name;
  protected VariableType type;
  
  public VariableId getId() {
    return id;
  }

  public void setId(VariableId id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }
  
  public Variable setName(String name) {
    this.name = name;
    return this;
  }
  
  public VariableType getType() {
    return type;
  }
  
  public Variable setType(VariableType type) {
    this.type = type;
    return this;
  }
}

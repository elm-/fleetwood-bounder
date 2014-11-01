/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.util;


/**
 * @author Tom Baeyens
 */
public class Id {

  protected Object state;
  
  public Id() {
  }
  
  public Id(Object state) {
    this.state = state;
  }

  public Object getState() {
    return state;
  }
  
  public void setState(Object state) {
    this.state = state;
  }

}

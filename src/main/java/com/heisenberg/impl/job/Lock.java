/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package com.heisenberg.impl.job;

import org.joda.time.LocalDateTime;


/**
 * @author Tom Baeyens
 */
// @Entity(value="locks", noClassnameStored=true)
public class Lock {

  public Lock() {
  }

  public Lock(String name) {
    this.time = new LocalDateTime();
    this.name = name;
  }
  
  public LocalDateTime time;
  public String name;
}

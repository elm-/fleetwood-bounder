/*
 * Copyright (c) 2013, Effektif GmbH.  All rights reserved.
 */

package funny.friday.util;



/**
 * @author Tom Baeyens
 */
public class Exceptions {

  public static void checkNotNull(Object object, String description) {
    if (object==null) {
      throw new RuntimeException(description+" is null");
    }
  }

}

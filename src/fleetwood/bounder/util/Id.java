/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package fleetwood.bounder.util;


/**
 * @author Walter White
 */
public class Id {

  protected Object internal;
  
  public Id(Object internal) {
    Exceptions.checkNotNull(internal, "internal");
    this.internal = internal;
  }

  public Object getInternal() {
    return internal;
  }
  
  public void setState(Object internal) {
    this.internal = internal;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((internal == null) ? 0 : internal.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Id other = (Id) obj;
    if (internal == null) {
      if (other.internal != null)
        return false;
    } else if (!internal.equals(other.internal))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return internal.toString();
  }

  public String getIdString() {
    return internal!=null ? internal.toString() : null;
  }
}

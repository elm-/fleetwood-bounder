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
package com.heisenberg.api.definition;


/** represent the location in the source file.
 * 
 * @author Walter White
 */
public class Location {
  
  public String file;
  public Long lineNumber;
  public String path;
  
  public Location file(String file) {
    this.file = file;
    return this;
  }
  
  public Location lineNumber(Long lineNumber) {
    this.lineNumber = lineNumber;
    return this;
  }

  /** path is the logical path in the process structure, If you ensure 
   * that the name of the elements are set first, then you'll get a 
   * default path. */
  public Location path(String path) {
    this.path = path;
    return this;
  }
  
  public String toString() {
    return (file!=null ? "file("+file+") " : "")+(lineNumber!=null ? "lineNumber("+lineNumber+") " : "")+(path!=null ? "path("+path+") " : "");
  }
}

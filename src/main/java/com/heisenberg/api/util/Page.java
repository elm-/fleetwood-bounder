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
package com.heisenberg.api.util;

import java.util.List;


/**
 * @author Walter White
 */
public interface Page<T> {
  
  // TODO check this paging http://sammaye.wordpress.com/2012/05/25/mongodb-paging-using-ranged-queries-avoiding-skip/
  
  // http://books.google.be/books?id=uGUKiNkKRJ0C&pg=PA70&lpg=PA70&dq=queries+without+skip&source=bl&ots=h8jzOjeRrh&sig=g-rfrn5aTofQ3VSv_cEbo6jaG58&hl=nl&sa=X&ei=cQVxVP-lD8nwaLj0gIgD&redir_esc=y#v=onepage&q=queries%20without%20skip&f=false
  // Avoiding Large Skips Using skip for a small number of documents is fine. Fora large number of results, 
  // skip can be slow, since it has to find and then discard all the skipped results. Most databases keep more 
  // metadata in the index to help with skips, but MongoDB does not yet support this, so large skips should be 
  // avoided. Often you can calculate the next query based on the result from the previous one. 
  // Paginating results without skip The easiest way to do pagination is to return the first page of results using 
  // limit and then return each subsequent page as an offset from the beginning: s n do not use: slow for large skips

  List<T> getResults();
  List<T> getNextPage();
  List<T> getPreviousPage();

}

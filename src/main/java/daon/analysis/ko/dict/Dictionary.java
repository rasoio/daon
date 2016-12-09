package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.CharsRef;

import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.Term;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Dictionary interface for retrieving morphological data
 * by id.
 */
public interface Dictionary {
  
  public static final String INTERNAL_SEPARATOR = "\u0000";
  
  public Map<Integer, List<Term>> lookup(char[] chars, int off, int len) throws IOException;

  public KeywordRef getKeywordRef(int idx);
  
  public Keyword getKeyword(long seq);
}

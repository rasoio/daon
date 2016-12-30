package daon.analysis.ko.dict;

import java.io.IOException;
import java.util.List;

import daon.analysis.ko.model.KeywordRef;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.tag.Tag;

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

	public List<Term> lookup(char[] chars, int off, int len) throws IOException;

	public KeywordRef getKeywordRef(int idx);
	
	public void setTag(Tag tag);
  
}

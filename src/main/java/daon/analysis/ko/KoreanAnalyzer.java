package daon.analysis.ko;

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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

import daon.analysis.ko.KoreanTokenizer.Mode;
import daon.analysis.ko.dict.UserDictionary;

/**
 * Analyzer for Japanese that uses morphological analysis.
 * @see KoreanTokenizer
 */
public class KoreanAnalyzer extends StopwordAnalyzerBase {
  private final Mode mode;
  private final Set<String> stoptags;
  private final UserDictionary userDict;
  
  public KoreanAnalyzer() {
    this(null, KoreanTokenizer.DEFAULT_MODE, DefaultSetHolder.DEFAULT_STOP_SET, DefaultSetHolder.DEFAULT_STOP_TAGS);
  }
  
  public KoreanAnalyzer(UserDictionary userDict, daon.analysis.ko.KoreanTokenizer.Mode defaultMode, CharArraySet stopwords, Set<String> stoptags) {
    super(stopwords);
    this.userDict = userDict;
    this.mode = defaultMode;
    this.stoptags = stoptags;
  }
  
  public static CharArraySet getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  
  public static Set<String> getDefaultStopTags(){
    return DefaultSetHolder.DEFAULT_STOP_TAGS;
  }
  
  /**
   * Atomically loads DEFAULT_STOP_SET, DEFAULT_STOP_TAGS in a lazy fashion once the 
   * outer class accesses the static final set the first time.
   */
  private static class DefaultSetHolder {
    static final CharArraySet DEFAULT_STOP_SET;
    static final Set<String> DEFAULT_STOP_TAGS;

    static {
      try {
        DEFAULT_STOP_SET = loadStopwordSet(true, KoreanAnalyzer.class, "stopwords.txt", "#");  // ignore case
        final CharArraySet tagset = loadStopwordSet(false, KoreanAnalyzer.class, "stoptags.txt", "#");
        DEFAULT_STOP_TAGS = new HashSet<>();
        for (Object element : tagset) {
          char chars[] = (char[]) element;
          DEFAULT_STOP_TAGS.add(new String(chars));
        }
      } catch (IOException ex) {
        // default set should always be present as it is part of the distribution (JAR)
        throw new RuntimeException("Unable to load default stopword or stoptag set");
      }
    }
  }
  
  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
//    Tokenizer tokenizer = new KoreanTokenizer(userDict, true, mode);
    Tokenizer tokenizer = new KeywordTokenizer();
    TokenStream stream = new StopFilter(tokenizer, stopwords);
//    stream = new JapanesePartOfSpeechStopFilter(stream, stoptags);
//    stream = new CJKWidthFilter(stream);
//    stream = new StopFilter(stream, stopwords);
//    stream = new JapaneseKatakanaStemFilter(stream);
    stream = new LowerCaseFilter(stream);
    return new TokenStreamComponents(tokenizer, stream);
  }
}

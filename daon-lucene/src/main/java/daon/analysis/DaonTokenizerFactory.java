package daon.analysis;


import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

/** 
 * Factory for {@link DaonTokenizer}.
 */
public class DaonTokenizerFactory extends TokenizerFactory {
  
  public DaonTokenizerFactory(Map<String,String> args) {
    super(args);
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }
  
  @Override
  public Tokenizer create(AttributeFactory factory) {
    return new DaonTokenizer(factory);
  }
}


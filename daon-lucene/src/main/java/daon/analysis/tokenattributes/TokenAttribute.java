package daon.analysis.tokenattributes;


import daon.analysis.Token;
import org.apache.lucene.util.Attribute;

/**
 * Attribute for .
 */
public interface TokenAttribute extends Attribute {
  Token getToken();
  void setToken(Token token);
}

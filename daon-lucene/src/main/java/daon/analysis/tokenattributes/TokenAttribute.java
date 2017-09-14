package daon.analysis.tokenattributes;


import daon.analysis.Token;
import org.apache.lucene.util.Attribute;

/**
 * Attribute for .
 */
public interface TokenAttribute extends Attribute {
  public Token getToken();
  public void setToken(Token token);
}

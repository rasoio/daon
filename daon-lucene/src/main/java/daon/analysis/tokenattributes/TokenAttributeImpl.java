package daon.analysis.tokenattributes;


import daon.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

/**
 */
public class TokenAttributeImpl extends AttributeImpl implements TokenAttribute, Cloneable {
  private Token token;


  @Override
  public Token getToken() {
    return token;
  }

  @Override
  public void setToken(Token token) {
    this.token = token;
  }

  @Override
  public void clear() {
    token = null;
  }

  @Override
  public void copyTo(AttributeImpl target) {
    TokenAttribute t = (TokenAttribute) target;
    t.setToken(token);
  }

  @Override
  public void reflectWith(AttributeReflector reflector) {
    reflector.reflect(TokenAttribute.class, "token", token);
  }

}

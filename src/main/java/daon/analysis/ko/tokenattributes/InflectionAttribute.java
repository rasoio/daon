package daon.analysis.ko.tokenattributes;

import org.apache.lucene.util.Attribute;

import daon.analysis.ko.Token;

/**
 * Attribute for Kuromoji inflection data.
 * <p>
 * Note: in some cases this value may not be applicable,
 * and will be null.
 */
public interface InflectionAttribute extends Attribute {
  public String getInflectionType();
  public String getInflectionForm();
  public void setToken(Token token);
}

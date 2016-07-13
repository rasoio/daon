package daon.analysis.ko.tokenattributes;

import org.apache.lucene.util.Attribute;

import daon.analysis.ko.Token;

/**
 * Attribute for Kuromoji reading data
 * <p>
 * Note: in some cases this value may not be applicable,
 * and will be null.
 */
public interface ReadingAttribute extends Attribute {
  public String getReading();
  public String getPronunciation();
  public void setToken(Token token);
}

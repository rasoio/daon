package daon.analysis.ko.tokenattributes;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

import daon.analysis.ko.Token;
import daon.analysis.ko.util.ToStringUtil;

/**
 * Attribute for Kuromoji inflection data.
 */
public class InflectionAttributeImpl extends AttributeImpl implements InflectionAttribute, Cloneable {
  private Token token;
  
  @Override
  public String getInflectionType() {
    return token == null ? null : token.getInflectionType();
  }
  
  @Override
  public String getInflectionForm() {
    return token == null ? null : token.getInflectionForm();
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
    InflectionAttribute t = (InflectionAttribute) target;
    t.setToken(token);
  }
  
  @Override
  public void reflectWith(AttributeReflector reflector) {
    String type = getInflectionType();
    String typeEN = type == null ? null : ToStringUtil.getInflectionTypeTranslation(type);
    reflector.reflect(InflectionAttribute.class, "inflectionType", type);
    reflector.reflect(InflectionAttribute.class, "inflectionType (en)", typeEN);
    String form = getInflectionForm();
    String formEN = form == null ? null : ToStringUtil.getInflectedFormTranslation(form);
    reflector.reflect(InflectionAttribute.class, "inflectionForm", form);
    reflector.reflect(InflectionAttribute.class, "inflectionForm (en)", formEN);
  }
}

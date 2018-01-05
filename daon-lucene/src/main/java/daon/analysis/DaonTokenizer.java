package daon.analysis;


import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

/**
 * 줄바꿈 문자 기준으로 분리
 */
public class DaonTokenizer extends CharTokenizer {

    /**
     * Construct a new DaonTokenizer.
     */
    public DaonTokenizer() {}

    /**
     * Construct a new DaonTokenizer using a given
     * {@link org.apache.lucene.util.AttributeFactory}.
     *
     * @param factory
     *          the attribute factory to use for this {@link Tokenizer}
     */
    public DaonTokenizer(AttributeFactory factory) {
        super(factory);
    }

    /**
     * Collects only characters which do not satisfy
     */
    @Override
    protected boolean isTokenChar(int c) {
        switch(c) {
            case 0x000D: //CARRIAGE RETURN
            case 0x000A: //LINE FEED
            case 0x0085: //NEXT LINE
            case 0x2028: //LINE SEPARATOR
            case 0x2029: //PARAGRAPH SEPARATOR
                return false;
            default:
                return true;
        }
    }
}

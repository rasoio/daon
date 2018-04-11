package daon.analysis;


import daon.core.Daon;
import daon.core.config.POSTag;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.Node;
import daon.core.util.Utils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


public final class DaonFilter extends TokenFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DaonFilter.class);

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class); // 같은 startOffset 에서 여러개 token 인 경우 첫번째만 1, 나머지 0
//    private final PositionLengthAttribute posLengthAtt = addAttribute(PositionLengthAttribute.class); // default : 1

    private final Daon daon;
    private final String mode;
    private long includeBit = -1;
    private long excludeBit = -1;

    private Queue<Token> tokenQueue = new LinkedList<>();

    private int startOffset;

    public DaonFilter(TokenStream input, String mode, List<String> include, List<String> exclude) {
        super(input);

        this.daon = new Daon();
        this.mode = mode;

        includeBit = Utils.makeTagBit(include);
        excludeBit = Utils.makeTagBit(exclude);
    }

    @Override
    public boolean incrementToken() throws IOException {

        if (moveAndPop()) return true;

        while (input.incrementToken()) {
            char[] termBuffer = termAtt.buffer();
            int termLength = termAtt.length();

            startOffset = offsetAtt.startOffset();

            TokenHandler handler = new TokenHandler();
            handler.setMode(mode);
            handler.setIncludeBit(includeBit);
            handler.setExcludeBit(excludeBit);
            handler.setStartOffset(startOffset);

            daon.analyzeWithHandler(termBuffer, termLength, handler);

            tokenQueue = handler.getList();

            if (moveAndPop()) return true;
        }

        return false;
    }

    private boolean moveAndPop() {

        if(!tokenQueue.isEmpty()){
            setAttributesFromQueue();
            return true;
        }

        return false;
    }

    private void setAttributesFromQueue() {
        clearAttributes();

        final Token token = tokenQueue.remove();

        termAtt.setEmpty().append(token.getTerm());
        offsetAtt.setOffset(token.getStartOffset(), token.getEndOffset());
        typeAtt.setType(token.getType());
        posIncAtt.setPositionIncrement(token.getPosInc());
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokenQueue.clear();
        startOffset = 0;
        includeBit = -1;
        excludeBit = -1;
    }
}

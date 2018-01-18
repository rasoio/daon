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

import java.io.IOException;
import java.util.*;


public final class DaonFilter extends TokenFilter {

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

        if(include != null && !include.isEmpty()){
            includeBit = 0;
            for (String tagName : include){
                POSTag tag = POSTag.valueOf(tagName);
                includeBit |= tag.getBit();
            }
        }

        if(exclude != null && !exclude.isEmpty()){
            excludeBit = 0;
            for (String tagName : exclude){
                POSTag tag = POSTag.valueOf(tagName);
                excludeBit |= tag.getBit();
            }
        }

    }

    @Override
    public boolean incrementToken() throws IOException {

        if (moveAndPop()) return true;

        while (input.incrementToken()) {
            char[] termBuffer = termAtt.buffer();
            int termLength = termAtt.length();

            startOffset = offsetAtt.startOffset();

            List<EojeolInfo> eojeolInfos = daon.analyze(termBuffer, termLength);

            for(EojeolInfo eojeol : eojeolInfos){
                addQueue(eojeol);
            }

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

    private void addQueue(EojeolInfo eojeol){

        String term = eojeol.getSurface();
        int offset = startOffset + eojeol.getOffset();
        int length = term.length();

        Token token = new Token(term, offset, length, "EOJEOL");

        addNodes(eojeol, token);
    }

    private void addNodes(EojeolInfo eojeol, Token eojeolToken) {
        //node's
        for(Node node : eojeol.getNodes()){
            String term = node.getSurface();
            int offset = startOffset + node.getOffset();
            int length = node.getLength();

            Token nodeToken = new Token(term, offset, length, "NODE");

            setPositionIncrement(eojeolToken, nodeToken);

            // 어절 term과 중복 되는 경우 어절 term 우선
            if(equalsToken(eojeolToken, nodeToken)) {
                nodeToken = null;
            }

            addKeywords(node, offset, eojeolToken, nodeToken);
        }
    }

    private void addKeywords(Node node, int firstOffset, Token eojeolToken, Token nodeToken) {
        //keyword's
        Keyword[] keywords = node.getKeywords();

        for (int i = 0, len = keywords.length; i <len; i++) {
            Keyword keyword = keywords[i];
            String term = keyword.getWord();
            int offset = firstOffset; // node's offset + w.length
            int length = term.length();
            if(i > 0){
                offset += length;
            }

            POSTag tag = keyword.getTag();
            String type = tag.getName();

            Token token = new Token(term, offset, length, type);

            // 어절, node 텀보다 태그텀이 우선
            if(i == 0){

                if("index".equals(mode)) {
                    addPriorityToken(eojeolToken, nodeToken, token);
                }

                addPosToken(token, tag);

            }else{
                addPosToken(token, tag);
            }
        }
    }

    private void addPosToken(Token token, POSTag tag) {

        if(includeBit == -1 && excludeBit == -1){
            tokenQueue.add(token);
        }else if(includeBit > -1) {
            if (Utils.containsTag(includeBit, tag)) {
                tokenQueue.add(token);
            }
        }else if(excludeBit > -1){
            if (!Utils.containsTag(excludeBit, tag)) {
                tokenQueue.add(token);
            }
        }

    }

    private void addPriorityToken(Token eojeolToken, Token nodeToken, Token token) {
        if(nodeToken == null){
            if(!equalsToken(eojeolToken, token)){
                tokenQueue.add(eojeolToken);

                setPositionIncrement(eojeolToken, token);
            }
        }else{
            setPositionIncrement(eojeolToken, token);

            tokenQueue.add(eojeolToken);
            if(!equalsToken(nodeToken, token)){
                tokenQueue.add(nodeToken);
            }
        }
    }

    private void setPositionIncrement(Token sourceToken, Token targetToken) {
        if(sourceToken.getStartOffset() == targetToken.getStartOffset()) {
            targetToken.setPosInc(0);
        }
    }


    private boolean equalsToken(Token token1, Token token2) {
        return token1.getStartOffset() == token2.getStartOffset() &&
                token1.getEndOffset() == token2.getEndOffset() &&
                token1.getTerm().equals(token2.getTerm());
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
    }
}

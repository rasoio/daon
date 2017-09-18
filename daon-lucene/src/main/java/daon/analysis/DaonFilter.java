package daon.analysis;


import daon.core.Daon;
import daon.core.model.EojeolInfo;
import daon.core.model.Keyword;
import daon.core.model.ModelInfo;
import daon.core.model.Node;
import daon.core.util.ModelUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public final class DaonFilter extends TokenFilter {



    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class); // 같은 startOffset 에서 여러개 token 인 경우 첫번째만 1, 나머지 0
//    private final PositionLengthAttribute posLengthAtt = addAttribute(PositionLengthAttribute.class);

    private Daon daon;

    private List<EojeolInfo> eojeolInfos = new ArrayList<>();

    private Queue<Token> tokenQueue = new LinkedList<>();

    private int index;
    private int startOffset;

    public DaonFilter(TokenStream input) {
        super(input);

        daon = new Daon();
    }

    @Override
    public boolean incrementToken() throws IOException {

        if(!tokenQueue.isEmpty()){
            setAttributesFromQueue();
            return true;
        }

        if (moveAndPop()) return true;

        while (input.incrementToken()) {
            char[] termBuffer = termAtt.buffer();
            int termLength = termAtt.length();

            startOffset = offsetAtt.startOffset();

            eojeolInfos = daon.analyze(termBuffer, termLength);

//            System.out.println("sentence : " + termAtt.toString());
//            System.out.println("startOffset : " + startOffset);
//
//            eojeolInfos.forEach(e -> {
//                System.out.println(e.getSurface());
//                e.getNodes().forEach(t -> {
//                    System.out.println(" '" + t.getSurface() + "' (" + t.getOffset() + ":" + (t.getOffset() + t.getLength()) + ")");
//                    for (Keyword k : t.getKeywords()) {
//                        System.out.println("     " + k);
//                    }
//                });
//            });

            index = 0;

            if (moveAndPop()) return true;
        }

        return false;
    }

    private boolean moveAndPop() {
        if(hasNext()){
            EojeolInfo eojeol = getNextEojeol();
            addQueue(eojeol);
        }

        if(!tokenQueue.isEmpty()){
            setAttributesFromQueue();
            return true;
        }

        return false;
    }

    private EojeolInfo getNextEojeol() {
        EojeolInfo eojeol = eojeolInfos.get(index);
        index++;

        return eojeol;
    }

    private void addQueue(EojeolInfo eojeol){

        //타입별 분기 처리

        //색인 시 : 어절, node, keyword
        //검색 시 : node ..?

        //어절 포층형
        String term = eojeol.getSurface();
        int offset = startOffset + eojeol.getOffset();
        int length = term.length();

        tokenQueue.add(new Token(term, offset, length, "EOJEOL"));

        addNodes(eojeol);
    }

    private void addNodes(EojeolInfo eojeol) {
        //node's
        for(Node node : eojeol.getNodes()){
            String term = node.getSurface();
            int offset = startOffset + node.getOffset();
            int length = node.getLength();

//            tokenQueue.add(new Token(term, offset, length));

            addKeywords(node, offset);
        }
    }

    private void addKeywords(Node node, int firstOffset) {
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

            String posTag = keyword.getTag().getName();
            tokenQueue.add(new Token(term, offset, length, posTag));
        }
    }

    private boolean hasNext(){
        int size = eojeolInfos.size();
        return size > 0 && index < size;
    }


    private void setAttributesFromQueue() {
        clearAttributes();

        final Token token = tokenQueue.remove();

        termAtt.setEmpty().append(token.getTerm());

        offsetAtt.setOffset(token.getStartOffset(), token.getEndOffset());
        typeAtt.setType(token.getType());
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        eojeolInfos.clear();
        tokenQueue.clear();
        index = 0;
        startOffset = 0;
    }
}

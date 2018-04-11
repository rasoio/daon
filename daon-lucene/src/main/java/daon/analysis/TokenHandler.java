package daon.analysis;

import daon.core.config.POSTag;
import daon.core.handler.AbstractHandler;
import daon.core.handler.EojeolInfoHandler;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TokenHandler extends AbstractHandler implements EojeolInfoHandler{

    private LinkedList<Token> list = new LinkedList<>();

    private String mode;

    private int startOffset;

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    @Override
    public void handle(EojeolInfo eojeolInfo) {
        addQueue(eojeolInfo);
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
        List<Node> nodes = eojeol.getNodes();
        for (int i = 0, len = nodes.size(); i <len; i++) {
            boolean isFirst = i == 0;
            Node node = nodes.get(i);
            String term = node.getSurface();
            int offset = startOffset + node.getOffset();
            int length = node.getLength();

            Token nodeToken = new Token(term, offset, length, "NODE");

            // 어절 term과 중복 되는 경우 어절 term 우선
            if(equalsToken(eojeolToken, nodeToken)) {
                nodeToken = null;
            }

            if(!isFirst){
                eojeolToken = null;
            }else{
                setPositionIncrement(eojeolToken, nodeToken);
            }

            addKeywords(node, offset, eojeolToken, nodeToken);
        }
    }

    private void addKeywords(Node node, int firstOffset, Token eojeolToken, Token nodeToken) {
        //keyword's
        Keyword[] keywords = node.getKeywords();

        String surface = node.getSurface();


        addToken(surface, keywords, firstOffset, nodeToken, eojeolToken);

    }

    private void addToken(String surface, Keyword[] keywords, int firstOffset, Token nodeToken, Token eojeolToken) {
        int keywordLength = keywords.length;

        int from = 0;
        int offset = firstOffset;
        for (int i = 0; i < keywordLength; i++) {
            boolean isFirst = i == 0;
            Keyword keyword = keywords[i];
            String term = keyword.getWord();
            int termLength = term.length();

            boolean isMatch = isMatch(surface, term, from);

            Token token = null;

            if(isMatch){
                POSTag tag = keyword.getTag();
                String type = tag.getName();

                token = new Token(term, offset, termLength, type);

                offset += termLength;
                from += termLength;
            //불규칙 조합 처리
            }else{
                if(surface.length() == from){
                    continue;
                }

                String partial = surface.substring(from);

                token = mergeIrrTerm(partial, keywords, i, offset, keywordLength);

                if(token != null) {
                    termLength = token.getLength();
                    offset += termLength;
                    from += termLength;
                    int move = token.getKeywords().size();

                    if (move > 0) move -= 1;

                    i += move;
                }
            }

            if(isFirst){
                // 어절, node 텀보다 태그텀이 우선
                if("index".equals(mode)) {
                    addPriorityToken(eojeolToken, nodeToken, token);
                }

                addPosToken(token);

            }else{
                addPosToken(token);
            }


        }
    }

    private boolean isMatch(String surface, String term, int from){
        int surfaceLength = surface.length();
        int termLength = term.length();

        int endIndex = from + termLength;
        if(surfaceLength < endIndex){
            return false;
        }

        return term.equals(surface.substring(from, endIndex));
    }


    private boolean isBackwardMatch(String surface, String term, int beginIndex){
        if(beginIndex < 0) return false;

        return term.equals(surface.substring(beginIndex));
    }


    private Token mergeIrrTerm(String surface, Keyword[] keywords, int startIdx, int offset, int keywordsLength){

        List<Keyword> irrKeywords = new ArrayList<>();

        //backward search
        int surfaceEnd = surface.length();
        int end = keywordsLength - 1;
        for(int i = end; i > startIdx; i--){
            Keyword keyword = keywords[i];
            String term = keyword.getWord();
            int termLength = term.length();

            int checkLength = surfaceEnd - termLength;
            boolean isMatch = isBackwardMatch(surface, term, checkLength);

            if(isMatch){
                surfaceEnd -= termLength;
                surface = surface.substring(0, surfaceEnd);
                end = i-1;
            }
        }

        String mergeSurface = surface;

        if(mergeSurface.isEmpty()){
            return null;
        }

        for (int i = startIdx; i <= end; i++) {
            Keyword keyword = keywords[i];
            irrKeywords.add(keyword);
        }

        int termLength = mergeSurface.length();

        String types = irrKeywords.stream().map(k->k.getTag().getName()).collect(Collectors.joining("+"));

        Token token = new Token(mergeSurface, offset, termLength, types);
        token.setKeywords(irrKeywords);

        return token;
    }

    private void addPosToken(Token token) {
        if(token != null){
            list.add(token);
        }
    }

    /**
     * 기본 token 보다 우선 적용되어야하는 token 적용
     * @param eojeolToken
     * @param nodeToken
     * @param token
     */
    private void addPriorityToken(Token eojeolToken, Token nodeToken, Token token) {
        if(eojeolToken == null) {
            if(!equalsToken(nodeToken, token)){
                list.add(nodeToken);
            }
        }else if(nodeToken == null){
            if(!equalsToken(eojeolToken, token)){
                list.add(eojeolToken);

                setPositionIncrement(eojeolToken, token);
            }
        }else{
            setPositionIncrement(eojeolToken, token);

            list.add(eojeolToken);
            if(!equalsToken(nodeToken, token)){
                list.add(nodeToken);
            }
        }
    }

    private void setPositionIncrement(Token sourceToken, Token targetToken) {
        if(targetToken != null && sourceToken.getStartOffset() == targetToken.getStartOffset()) {
            targetToken.setPosInc(0);
        }
    }


    private boolean equalsToken(Token token1, Token token2) {
        return token1 != null && token2 != null &&
                token1.getStartOffset() == token2.getStartOffset() &&
                token1.getEndOffset() == token2.getEndOffset() &&
                token1.getTerm().equals(token2.getTerm());
    }

    @Override
    public LinkedList<Token> getList() {
        return list;
    }
}

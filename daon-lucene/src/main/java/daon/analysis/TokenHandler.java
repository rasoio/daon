package daon.analysis;

import daon.core.config.POSTag;
import daon.core.handler.AbstractHandler;
import daon.core.handler.EojeolInfoHandler;
import daon.core.result.EojeolInfo;
import daon.core.result.Keyword;
import daon.core.result.Node;

import java.util.LinkedList;
import java.util.List;

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

            setPositionIncrement(eojeolToken, nodeToken);

            // 어절 term과 중복 되는 경우 어절 term 우선
            if(equalsToken(eojeolToken, nodeToken)) {
                nodeToken = null;
            }

            if(!isFirst){
                eojeolToken = null;
            }

            addKeywords(node, offset, eojeolToken, nodeToken);
        }
    }

    private void addKeywords(Node node, int firstOffset, Token eojeolToken, Token nodeToken) {
        //keyword's
        Keyword[] keywords = node.getKeywords();

        for (int i = 0, len = keywords.length; i <len; i++) {
            boolean isFirst = i == 0;
            Keyword keyword = keywords[i];
            String term = keyword.getWord();
            int offset = firstOffset; // node's offset + w.length
            int length = term.length();

            if(!isFirst){
                offset += length;
            }

            POSTag tag = keyword.getTag();
            String type = tag.getName();

            Token token = new Token(term, offset, length, type);

            if(!isValid(keyword)){
                token = null;
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

    private void addPosToken(Token token) {
        if(token != null){
            list.add(token);
        }
    }

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

package daon.analysis;

import daon.core.result.Keyword;

import java.util.List;

public class Token {

    private String term;
    private int startOffset;
    private int length;
    private int endOffset;
    private String type;
    private int posInc = 1;
    private List<Keyword> keywords;

    public Token(String term, int offset, int length, String type) {
        this.term = term;
        this.startOffset = offset;
        this.length = length;
        this.endOffset = startOffset + length;
        this.type = type;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPosInc() {
        return posInc;
    }

    public void setPosInc(int posInc) {
        this.posInc = posInc;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }
}

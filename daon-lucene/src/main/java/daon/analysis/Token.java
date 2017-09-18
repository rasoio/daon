package daon.analysis;

public class Token {

    private String term;
    private int startOffset;
    private int length;
    private int endOffset;
    private String type;
    private int posInc;

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
}

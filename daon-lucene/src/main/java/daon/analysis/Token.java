package daon.analysis;

public class Token {

    private String term;
    private int startOffset;
    private int length;
    private int endOffset;
    private String type;

    public Token(String term, int offset, int length) {
        this.term = term;
        this.startOffset = offset;
        this.length = length;
        this.endOffset = startOffset + length;
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
}

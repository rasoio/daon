package daon.analysis.ko.model;

import daon.analysis.ko.util.Utils;

public class NextInfo {

    private Keyword next;

    private String nextWord;

    private char[] nextStart;

    public NextInfo(Keyword next) {
        this.next = next;

        nextWord = next.getWord();

        nextStart = Utils.getCharAtDecompose(next, 0);
    }

    public Keyword getNext() {
        return next;
    }

    public void setNext(Keyword next) {
        this.next = next;
    }

    public String getNextWord() {
        return nextWord;
    }

    public void setNextWord(String nextWord) {
        this.nextWord = nextWord;
    }

    public char[] getNextStart() {
        return nextStart;
    }

    public void setNextStart(char[] nextStart) {
        this.nextStart = nextStart;
    }

}

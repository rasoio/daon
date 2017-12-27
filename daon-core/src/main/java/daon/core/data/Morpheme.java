package daon.core.data;

import java.io.Serializable;


public class Morpheme implements Serializable {

    private int seq;

    private String word;

    private String tag;

    public Morpheme() {
    }

    public Morpheme(String word, String tag) {
        this.word = word;
        this.tag = tag;
    }

    public Morpheme(int seq, String word, String tag) {
        this.seq = seq;
        this.word = word;
        this.tag = tag;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {

        return "(seq : " + seq + ", word : " + word + ", tag : " + tag + ")";
    }


}

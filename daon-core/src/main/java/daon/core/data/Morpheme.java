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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Morpheme morpheme = (Morpheme) o;

        if (seq != morpheme.seq) return false;
        if (word != null ? !word.equals(morpheme.word) : morpheme.word != null) return false;
        return tag != null ? tag.equals(morpheme.tag) : morpheme.tag == null;
    }

    @Override
    public int hashCode() {
        int result = seq;
        result = 31 * result + (word != null ? word.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }
}

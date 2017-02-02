package daon.dictionary.model;

import daon.analysis.ko.dict.config.Config;

/**
 * Created by mac on 2017. 1. 24..
 */
public class Morpheme implements Comparable<Morpheme>{

    private long seq;

    private String word;

    private Config.POSTag tag;

    /**
     * outer - 어절간
     */
    private Morpheme prevOuter;
    private Morpheme nextOuter;

    /**
     * inner - 어절내
     */
    private Morpheme prevInner;
    private Morpheme nextInner;


    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Config.POSTag getTag() {
        return tag;
    }

    public void setTag(Config.POSTag tag) {
        this.tag = tag;
    }

    public Morpheme getPrevOuter() {
        return prevOuter;
    }

    public void setPrevOuter(Morpheme prevOuter) {
        this.prevOuter = prevOuter;
    }

    public Morpheme getNextOuter() {
        return nextOuter;
    }

    public void setNextOuter(Morpheme nextOuter) {
        this.nextOuter = nextOuter;
    }

    public Morpheme getPrevInner() {
        return prevInner;
    }

    public void setPrevInner(Morpheme prevInner) {
        this.prevInner = prevInner;
    }

    public Morpheme getNextInner() {
        return nextInner;
    }

    public void setNextInner(Morpheme nextInner) {
        this.nextInner = nextInner;
    }


    public Morpheme copy(){
        Morpheme copied = new Morpheme();
        copied.setWord(this.word);
        copied.setTag(this.tag);

        return copied;
    }

    @Override
    public int compareTo(Morpheme other) {
        return this.getWord().compareTo(other.getWord());
    }

    @Override
    public String toString() {
        return "Morpheme{" +
                "seq=" + seq +
                ", word='" + word + '\'' +
                ", tag=" + tag +
                ", prevOuter=" + prevOuter +
                ", nextOuter=" + nextOuter +
                ", prevInner=" + prevInner +
                ", nextInner=" + nextInner +
                '}';
    }
}

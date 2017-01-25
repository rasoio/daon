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
     * inter - 어절간
     */
    private Morpheme prevInter;
    private Morpheme nextInter;

    /**
     * intra - 어절내
     */
    private Morpheme prevIntra;
    private Morpheme nextIntra;


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

    public Morpheme getPrevInter() {
        return prevInter;
    }

    public void setPrevInter(Morpheme prevInter) {
        this.prevInter = prevInter;
    }

    public Morpheme getNextInter() {
        return nextInter;
    }

    public void setNextInter(Morpheme nextInter) {
        this.nextInter = nextInter;
    }

    public Morpheme getPrevIntra() {
        return prevIntra;
    }

    public void setPrevIntra(Morpheme prevIntra) {
        this.prevIntra = prevIntra;
    }

    public Morpheme getNextIntra() {
        return nextIntra;
    }

    public void setNextIntra(Morpheme nextIntra) {
        this.nextIntra = nextIntra;
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
                ", prevInter=" + prevInter +
                ", nextInter=" + nextInter +
                ", prevIntra=" + prevIntra +
                ", nextIntra=" + nextIntra +
                '}';
    }
}

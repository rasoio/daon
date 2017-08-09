package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;

import java.io.Serializable;


public class Keyword implements Serializable {

    /**
     * 사전 단어 구분 키값
     */
    private int seq;

    /**
     * 사전 단어
     */
    private String word;

    /**
     * 사전 단어 추가 정보
     * POS tag 정보 목록
     */
    private POSTag tag;

    /**
     * 사전 단어 사용 빈도
     */
    private long freq;

    /**
     * 단어 설명
     */
    private String desc = "";

    public Keyword() {
    }

    public Keyword(String word, POSTag tag) {
        this.word = word;
        this.tag = tag;
    }

    public Keyword(int seq, String word, POSTag tag) {
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

    public POSTag getTag() {
        return tag;
    }

    public void setTag(POSTag tag) {
        this.tag = tag;
    }

    public long getFreq() {
        return freq;
    }

    public void setFreq(long freq) {
        this.freq = freq;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getLength(){
        return word.length();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keyword keyword = (Keyword) o;

        if (seq != keyword.seq) return false;
        if (!word.equals(keyword.word)) return false;
        return tag == keyword.tag;
    }

    @Override
    public int hashCode() {
        int result = seq;
        result = 31 * result + word.hashCode();
        result = 31 * result + tag.hashCode();
        return result;
    }

    @Override
    public String toString() {

        return "(seq : " + seq + ", word : " + word + ", tag : " + tag + ", freq : " + freq + ")";
    }


}

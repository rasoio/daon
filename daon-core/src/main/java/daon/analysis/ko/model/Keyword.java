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
     * 사전 단어 사용 빈도
     */
    private float prob;

    /**
     * 중의어 구분 어깨번호
     */
    private String num = "";

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

    public float getProb() {
        return prob;
    }

    public void setProb(float prob) {
        this.prob = prob;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
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
    public String toString() {

        return "(seq : " + seq + ", word : " + word + ", tag : " + tag + ", freq : " + freq
//        return "(seq : " + seq + ", word : " + word + ", tag : " + tag + ", freq : " + freq
//        return "(seq : " + seq + ", word : " + word + ", tag : " + tag + ", freq : " + String.format("%.10f", prob)
//				+ ", freq=" + freq + ", desc=" + desc + ", subWords=" + subWords
//				+ ", tagBits=" + StringUtils.leftPad(Long.toBinaryString(tagBits), 64,"0")
                + ")";
    }


}

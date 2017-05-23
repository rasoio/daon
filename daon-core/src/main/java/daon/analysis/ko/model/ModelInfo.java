package daon.analysis.ko.model;

import daon.analysis.ko.fst.DaonFST;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ModelInfo {

    private long maxFreq;

    private DaonFST dictionaryFst;
    private DaonFST innerWordFst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();
    private Map<Integer, Float> inner = new HashMap<>();
    private Map<Integer, Float> outer = new HashMap<>();
    private Map<Integer, Float> tags = new HashMap<>();
    private Map<Integer, Float> tagTrans = new HashMap<>();

    public ModelInfo() {
    }

    public long getMaxFreq() {
        return maxFreq;
    }

    public void setMaxFreq(long maxFreq) {
        this.maxFreq = maxFreq;
    }

    public DaonFST getDictionaryFst() {
        return dictionaryFst;
    }

    public void setDictionaryFst(DaonFST dictionaryFst) {
        this.dictionaryFst = dictionaryFst;
    }

    public DaonFST getInnerWordFst() {
        return innerWordFst;
    }

    public void setInnerWordFst(DaonFST innerWordFst) {
        this.innerWordFst = innerWordFst;
    }

    public Map<Integer, Keyword> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<Integer, Keyword> dictionary) {
        this.dictionary = dictionary;
    }

    public Map<Integer, Float> getInner() {
        return inner;
    }

    public void setInner(Map<Integer, Float> inner) {
        this.inner = inner;
    }

    public Map<Integer, Float> getOuter() {
        return outer;
    }

    public void setOuter(Map<Integer, Float> outer) {
        this.outer = outer;
    }

    public Map<Integer, Float> getTags() {
        return tags;
    }

    public void setTags(Map<Integer, Float> tags) {
        this.tags = tags;
    }

    public Map<Integer, Float> getTagTrans() {
        return tagTrans;
    }

    public void setTagTrans(Map<Integer, Float> tagTrans) {
        this.tagTrans = tagTrans;
    }


    public Keyword getKeyword(int seq){
        return dictionary.get(seq);
    }




    public float getTagScore(Keyword keyword, Keyword nKeyword){

        float score = 0;

        if(keyword != null && nKeyword != null){
            String tag = keyword.getTag().name();
            String nTag = nKeyword.getTag().name();

            String key = tag + "|" + nTag;

            Float freq = getTagTrans().get(key.hashCode());

            if(freq != null) {
                score = freq;
            }
        }

        return score;
    }


    public float getTagScore(Keyword keyword){

        float score = 0;

        if(keyword != null){
            Integer key = keyword.getTag().name().hashCode();

            Float freq = getTags().get(key);

            if(freq != null) {
                score = freq / 100;
            }
        }

        return score;
    }
}

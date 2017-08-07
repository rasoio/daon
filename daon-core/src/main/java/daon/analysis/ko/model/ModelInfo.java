package daon.analysis.ko.model;

import daon.analysis.ko.fst.DaonFST;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.FST;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ModelInfo {

    private long maxFreq;

    private DaonFST<IntsRef> userFst;
    private DaonFST<Object> wordFst;
    private FST<Long> connFst;
    private FST<Long> innerFst;
//    private FST<Long> outerFst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();
//    private Map<Integer, Float> inner = new HashMap<>();
//    private Map<Integer, Float> outer = new HashMap<>();
//    private Map<Integer, Float> tags = new HashMap<>();
    private Map<Integer, Integer> tagTrans = new HashMap<>();

    public ModelInfo() {
    }

    public long getMaxFreq() {
        return maxFreq;
    }

    public void setMaxFreq(long maxFreq) {
        this.maxFreq = maxFreq;
    }

    public DaonFST<IntsRef> getUserFst() {
        return userFst;
    }

    public void setUserFst(DaonFST<IntsRef> userFst) {
        this.userFst = userFst;
    }

    public DaonFST<Object> getWordFst() {
        return wordFst;
    }

    public void setWordFst(DaonFST<Object> wordFst) {
        this.wordFst = wordFst;
    }

    public Map<Integer, Keyword> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<Integer, Keyword> dictionary) {
        this.dictionary = dictionary;
    }

    public FST<Long> getConnFst() {
        return connFst;
    }

    public void setConnFst(FST<Long> connFst) {

        this.connFst = connFst;
    }

    public FST<Long> getInnerFst() {
        return innerFst;
    }

    public void setInnerFst(FST<Long> innerFst) {
        this.innerFst = innerFst;
    }

//    public FST<Long> getOuterFst() {
//        return outerFst;
//    }

//    public void setOuterFst(FST<Long> outerFst) {
//        this.outerFst = outerFst;
//    }

    public Map<Integer, Integer> getTagTrans() {
        return tagTrans;
    }

    public void setTagTrans(Map<Integer, Integer> tagTrans) {
        this.tagTrans = tagTrans;
    }


    public Keyword getKeyword(int seq){
        return dictionary.get(seq);
    }


    public int getTagScore(String t1, String t2){

        int score = 4000;

        String key = t1 + "|" + t2;

        Integer freq = getTagTrans().get(key.hashCode());

        if(freq != null) {
            score = freq;
        }

        return score;
    }

}

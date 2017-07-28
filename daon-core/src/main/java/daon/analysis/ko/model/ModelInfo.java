package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.DaonFST;
import daon.analysis.ko.processor.ConnectionFinder;
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
    private DaonFST<Object> forwardFst;
    private DaonFST<Object> backwardFst;
    private FST<Long> connFst;
    private FST<Long> innerFst;
//    private FST<Long> outerFst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();
//    private Map<Integer, Float> inner = new HashMap<>();
//    private Map<Integer, Float> outer = new HashMap<>();
//    private Map<Integer, Float> tags = new HashMap<>();
    private Map<Integer, Float> tagTrans = new HashMap<>();

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

    public DaonFST<Object> getForwardFst() {
        return forwardFst;
    }

    public void setForwardFst(DaonFST<Object> forwardFst) {
        this.forwardFst = forwardFst;
    }

    public DaonFST<Object> getBackwardFst() {
        return backwardFst;
    }

    public void setBackwardFst(DaonFST<Object> backwardFst) {
        this.backwardFst = backwardFst;
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

    public Map<Integer, Float> getTagTrans() {
        return tagTrans;
    }

    public void setTagTrans(Map<Integer, Float> tagTrans) {
        this.tagTrans = tagTrans;
    }


    public Keyword getKeyword(int seq){
        return dictionary.get(seq);
    }


    public float getTagScore(String t1, String t2){

        float score = 0;

        String key = t1 + "|" + t2;

        Float freq = getTagTrans().get(key.hashCode());

        if(freq != null) {
            score = freq;
        }

        return score;
    }

}

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
    private DaonFST<Object> wordsFst;
    private FST<Long> connFst;

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

    public DaonFST<Object> getWordsFst() {
        return wordsFst;
    }

    public void setWordsFst(DaonFST<Object> wordsFst) {
        this.wordsFst = wordsFst;
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


    public float getTagScore(POSTag t1, POSTag t2){

        float score = 0;

        String tag1 = t1.name();
        String tag2 = t2.name();

        String key = tag1 + "|" + tag2;

        Float freq = getTagTrans().get(key.hashCode());

        if(freq != null) {
            score = freq;
        }

        return score;
    }

}

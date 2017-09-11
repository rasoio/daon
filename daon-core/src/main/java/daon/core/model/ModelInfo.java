package daon.core.model;

import daon.core.fst.DaonFST;
import lucene.core.util.IntsRef;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ModelInfo {

    private DaonFST<IntsRef> userFst;
    private DaonFST<Object> wordFst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();

    private Integer[] firstTags = new Integer[100];
    private Integer[][] middleTags = new Integer[100][100];
    private Integer[] lastTags = new Integer[100];
    private Integer[][] connectTags = new Integer[100][100];

    public ModelInfo() {}

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

    public Integer[] getFirstTags() {
        return firstTags;
    }

    public void setFirstTags(Integer[] firstTags) {
        this.firstTags = firstTags;
    }

    public Integer[][] getMiddleTags() {
        return middleTags;
    }

    public void setMiddleTags(Integer[][] middleTags) {
        this.middleTags = middleTags;
    }

    public Integer[] getLastTags() {
        return lastTags;
    }

    public void setLastTags(Integer[] lastTags) {
        this.lastTags = lastTags;
    }

    public Integer[][] getConnectTags() {
        return connectTags;
    }

    public void setConnectTags(Integer[][] connectTags) {
        this.connectTags = connectTags;
    }

    public Keyword getKeyword(int seq){
        return dictionary.get(seq);
    }


}

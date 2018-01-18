package daon.core.result;

import daon.core.fst.DaonFST;
import daon.core.fst.DaonFSTBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by mac on 2017. 5. 18..
 */
public class ModelInfo {

    private Logger logger = LoggerFactory.getLogger(ModelInfo.class);

    private DaonFST<Object> wordFst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();

    private Integer[] firstTags = new Integer[100];
    private Integer[][] middleTags = new Integer[100][100];
    private Integer[] lastTags = new Integer[100];
    private Integer[][] connectTags = new Integer[100][100];

    //statistics
    private String target;
    private int dictionarySize;
    private Date loadedDate;
    private long elapsed;
    private boolean isSuccess;
    private String errorMsg;

    public ModelInfo() {
        try {
            List<KeywordIntsRef> list = new ArrayList<>();
            list.add(new KeywordIntsRef("", new int[]{0}));
            wordFst = DaonFSTBuilder.create().buildPairFst(list);
        } catch (IOException e) {
            logger.error("모델 초기화 에러", e);
        }
    }

    public DaonFST<Object> getWordFst() {
        return wordFst;
    }

    public void setWordFst(DaonFST<Object> wordFst) {
        this.wordFst = wordFst;
    }

    public void createDictionary(int initialCapacity){
        dictionary = new HashMap<>(initialCapacity);
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

    public Integer[][] getMiddleTags() {
        return middleTags;
    }

    public Integer[] getLastTags() {
        return lastTags;
    }

    public Integer[][] getConnectTags() {
        return connectTags;
    }

    public Keyword getKeyword(int seq){
        return dictionary.get(seq);
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getDictionarySize() {
        return dictionarySize;
    }

    public void setDictionarySize(int dictionarySize) {
        this.dictionarySize = dictionarySize;
    }

    public Date getLoadedDate() {
        return loadedDate;
    }

    public void setLoadedDate(Date loadedDate) {
        this.loadedDate = loadedDate;
    }

    public long getElapsed() {
        return elapsed;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}

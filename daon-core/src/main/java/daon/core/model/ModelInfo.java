package daon.core.model;

import daon.core.fst.DaonFST;
import daon.core.fst.DaonFSTBuilder;
import daon.core.reader.ModelReader;
import lucene.core.util.IntsRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


}

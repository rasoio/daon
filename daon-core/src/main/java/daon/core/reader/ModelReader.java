package daon.core.reader;

import com.google.protobuf.CodedInputStream;
import daon.core.config.POSTag;
import daon.core.fst.DaonFST;
import daon.core.fst.DaonFSTBuilder;
import daon.core.result.Keyword;
import daon.core.result.ModelInfo;
import daon.core.proto.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
public class ModelReader {

    private Logger logger = LoggerFactory.getLogger(ModelReader.class);

    private String filePath = null;
    private String url = null;
    private int timeout = 30000; // default 30 sec
    private InputStream inputStream = null;

    private String target = null;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public static ModelReader create() {

        return new ModelReader();
    }

    private ModelReader() {}

    public ModelReader filePath(String path){
        this.filePath = path;
        return this;
    }

    public ModelReader url(String url){
        this.url = url;
        return this;
    }

    public ModelReader inputStream(InputStream inputStream){
        this.inputStream = inputStream;
        return this;
    }

    public ModelReader timeout(int timeout){
        this.timeout = timeout;
        return this;
    }

    public ModelInfo load() {

        ModelInfo modelInfo = new ModelInfo();

        try {
            long start = System.currentTimeMillis();

            Model model = loadModel();

            initDictionary(model, modelInfo);

            initWordFst(model, modelInfo);

            initTags(model, modelInfo);

            long end = System.currentTimeMillis();

            long elapsed = (end - start);

            setSuccessInfo(modelInfo, model, elapsed);

            logger.info("model load elapsed : {} ms", elapsed);
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            setErrorMessage(modelInfo, errorMsg);

            logger.error("모델 load 시 에러 발생", e);
        }

        return modelInfo;
    }

    private void setErrorMessage(ModelInfo modelInfo, String errorMsg) {
        modelInfo.setSuccess(false);
        modelInfo.setErrorMsg(errorMsg);
        modelInfo.setTarget(target);
    }

    private void setSuccessInfo(ModelInfo modelInfo, Model model, long elapsed){

        modelInfo.setSuccess(true);
        modelInfo.setTarget(target);
        modelInfo.setDictionarySize(model.getDictionaryCount());
        modelInfo.setLoadedDate(new Date());
        modelInfo.setElapsed(elapsed);
    }

    private Model loadModel() throws Exception {
        InputStream inputStream = getInputStream();

        CodedInputStream input = CodedInputStream.newInstance(inputStream);

        input.setSizeLimit(Integer.MAX_VALUE);

        return Model.parseFrom(input);
    }

    private void initDictionary(Model model, ModelInfo modelInfo) {
        Map<Integer, Model.Keyword> dictionary = model.getDictionaryMap();

        modelInfo.createDictionary(dictionary.size());
        Map<Integer, Keyword> map = modelInfo.getDictionary();

        dictionary.forEach((key, value) -> {

            Keyword keyword = new Keyword();
            keyword.setSeq(value.getSeq());
            keyword.setWord(value.getWord());
            keyword.setTag(POSTag.valueOf(value.getTag()));

            map.put(key, keyword);
        });
    }

    private void initWordFst(Model model, ModelInfo modelInfo) throws IOException {
        byte[] wordBytes = model.getWordFst().toByteArray();

        DaonFST<Object> wordFst = DaonFSTBuilder.create().buildPairFst(wordBytes);

        modelInfo.setWordFst(wordFst);

    }

    private void initTags(Model model, ModelInfo modelInfo) {
        initFirstTags(model, modelInfo);

        initMiddleTags(model, modelInfo);

        initLastTags(model, modelInfo);

        initConnectTags(model, modelInfo);
    }

    private void initFirstTags(Model model, ModelInfo modelInfo) {
        List<String> firstTags = model.getFirstTagsList();

        for(String firstTag : firstTags){
            String[] tags = firstTag.split(",");
            int idx = POSTag.valueOf(tags[0]).getIdx();
            int cost = Integer.valueOf(tags[1]);

            modelInfo.getFirstTags()[idx] = cost;
        }
    }

    private void initMiddleTags(Model model, ModelInfo modelInfo) {
        List<String> middleTags = model.getMiddleTagsList();

        for(String middleTag : middleTags){
            String[] tags = middleTag.split(",");
            int idx1 = POSTag.valueOf(tags[0]).getIdx();
            int idx2 = POSTag.valueOf(tags[1]).getIdx();
            int cost = Integer.valueOf(tags[2]);

            modelInfo.getMiddleTags()[idx1][idx2] = cost;
        }
    }

    private void initLastTags(Model model, ModelInfo modelInfo) {
        List<String> lastTags = model.getLastTagsList();

        for(String lastTag : lastTags){
            String[] tags = lastTag.split(",");
            int idx = POSTag.valueOf(tags[0]).getIdx();
            int cost = Integer.valueOf(tags[1]);

            modelInfo.getLastTags()[idx] = cost;
        }
    }

    private void initConnectTags(Model model, ModelInfo modelInfo) {
        List<String> connectTags = model.getConnectTagsList();

        for(String connectTag : connectTags){
            String[] tags = connectTag.split(",");
            int idx1 = POSTag.valueOf(tags[0]).getIdx();
            int idx2 = POSTag.valueOf(tags[1]).getIdx();
            int cost = Integer.valueOf(tags[2]);

            modelInfo.getConnectTags()[idx1][idx2] = cost;
        }
    }

    private InputStream getInputStream() throws Exception {

        InputStream inputStream = null;

        if(filePath != null){
            target = filePath;
            inputStream = new FileInputStream(filePath);
        }else if(url != null){
            target = url;
            URLConnection connection = new URL(url).openConnection();
            connection.setConnectTimeout(3000); // connection timeout...
            connection.setReadTimeout(timeout);
            inputStream = connection.getInputStream();
        }else if(this.inputStream != null){
            target = "inputStream";
            inputStream = this.inputStream;
        }else{
            //default
            target = "default";
            inputStream = this.getClass().getResourceAsStream("model.dat");
        }

        return inputStream;
    }

}

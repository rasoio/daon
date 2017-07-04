package daon.analysis.ko.reader;

import com.google.protobuf.CodedInputStream;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.DaonFST;
import daon.analysis.ko.fst.DaonFSTBuilder;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.proto.Model;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.util.fst.FST;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
public class ModelReader {

    private Logger logger = LoggerFactory.getLogger(ModelReader.class);

    private String filePath = null;
    private String url = null;
    private InputStream inputStream = null;

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

    public ModelInfo load() throws IOException {

        StopWatch watch = new StopWatch();

        watch.start();

        InputStream inputStream = getInputStream();

//        CodedInputStream input = CodedInputStream.newInstance(new GZIPInputStream(new FileInputStream("/Users/mac/work/corpus/model/model.dat.gz")));
        CodedInputStream input = CodedInputStream.newInstance(inputStream);

        input.setSizeLimit(Integer.MAX_VALUE);

        Model model = Model.parseFrom(input);

        watch.stop();

        logger.info("model protobuf load elapsed : {} ms", watch.getTime());

        watch.reset();
        watch.start();


        byte[] userBytes = model.getUserFst().toByteArray();
        byte[] wordsBytes = model.getWordsFst().toByteArray();
        byte[] connBytes = model.getConnectionFst().toByteArray();

//        DaonFST userFst = DaonFSTBuilder.create().buildIntsFst(userBytes);
        DaonFST wordsFst = DaonFSTBuilder.create().buildPairFst(wordsBytes);
        FST<Object> connFst = DaonFSTBuilder.create().buildFst(connBytes);

        ModelInfo modelInfo = new ModelInfo();

//        modelInfo.setUserFst(dictionaryFst);
        modelInfo.setWordsFst(wordsFst);
        modelInfo.setConnFst(connFst);

        Map<Integer, Model.Keyword> dictionary = model.getDictionaryMap();

        long maxFreq = model.getMaxFreq();

        dictionary.entrySet().forEach(entry -> {

            Model.Keyword k = entry.getValue();

            Keyword r = new Keyword();
            r.setSeq(k.getSeq());
            r.setWord(k.getWord());
            r.setTag(POSTag.valueOf(k.getTag()));
            r.setFreq(k.getFreq());
            r.setProb((float) k.getFreq() / maxFreq);

            modelInfo.getDictionary().put(entry.getKey(), r);
        });

//        modelInfo.setInner(new HashMap<>(model.getInnerMap()));
//        modelInfo.setOuter(new HashMap<>(model.getOuterMap()));
//        modelInfo.setTags(new HashMap<>(model.getTagsMap()));

        Map<Integer, Float> tagTransMap = model.getTagTransMap();

        tagTransMap.entrySet().forEach(e -> {
            double score = Math.sqrt(Math.sqrt(Math.sqrt(Math.sqrt(Math.sqrt(e.getValue())))));
            modelInfo.getTagTrans().put(e.getKey(), (float) score);
        });
//        modelInfo.setTagTrans(new HashMap<>(model.getTagTransMap()));

        modelInfo.setMaxFreq(maxFreq);


        watch.stop();

        logger.info("dic cnt : {}, tagTrans cnt : {}",
                modelInfo.getDictionary().size(), modelInfo.getTagTrans().size());

        logger.info("max freq : {}", maxFreq );
        logger.info("model load elapsed : {} ms", watch.getTime() );


//        logger.info("model dictionaryFst size : {} byte", dictionaryFst.getInternalFST().ramBytesUsed() );
        logger.info("model wordsFst size : {} byte", wordsFst.getInternalFST().ramBytesUsed());
        logger.info("model connFst size : {} byte", connFst.ramBytesUsed());

        return modelInfo;
    }

    private InputStream getInputStream() throws IOException {

        InputStream inputStream = null;

        if(filePath != null){
            inputStream = new FileInputStream(filePath);
        }

        if(url != null){
            inputStream = new URL(url).openStream();
        }

        if(this.inputStream != null){
            inputStream = this.inputStream;
        }

        if(inputStream == null){
            inputStream = this.getClass().getResourceAsStream("model.dat");
        }

        return inputStream;
    }

}

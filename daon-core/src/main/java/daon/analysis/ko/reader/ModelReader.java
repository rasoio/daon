package daon.analysis.ko.reader;

import com.google.protobuf.CodedInputStream;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.DaonFST;
import daon.analysis.ko.fst.DaonFSTBuilder;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ModelInfo;
import daon.analysis.ko.proto.Model;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
public class ModelReader {

    private Logger logger = LoggerFactory.getLogger(ModelReader.class);

    private String path = "/Users/mac/work/corpus/model/model2.dat";

    public static ModelReader create() {

        return new ModelReader();
    }


    private ModelReader() {}

    public ModelReader path(String path){
        this.path = path;
        return this;
    }

    public ModelInfo load() throws IOException {

        StopWatch watch = new StopWatch();

        watch.start();

//        CodedInputStream input = CodedInputStream.newInstance(new GZIPInputStream(new FileInputStream("/Users/mac/work/corpus/model/model.dat.gz")));
        CodedInputStream input = CodedInputStream.newInstance(new FileInputStream(path));

        input.setSizeLimit(Integer.MAX_VALUE);

        Model model = Model.parseFrom(input);

        watch.stop();

        logger.info("model protobuf load elapsed : {} ms", watch.getTime() );

        watch.reset();
        watch.start();


        byte[] dictionaryBytes = model.getDictionaryFst().toByteArray();
        byte[] innerWordBytes = model.getInnerWordFst().toByteArray();

        DaonFST dictionaryFst = DaonFSTBuilder.create().buildIntsFst(dictionaryBytes);
        DaonFST innerWordFst = DaonFSTBuilder.create().buildPairFst(innerWordBytes);

        ModelInfo modelInfo = new ModelInfo();

        modelInfo.setDictionaryFst(dictionaryFst);
        modelInfo.setInnerWordFst(innerWordFst);

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

        modelInfo.setInner(new HashMap<>(model.getInnerMap()));
        modelInfo.setOuter(new HashMap<>(model.getOuterMap()));
        modelInfo.setTags(new HashMap<>(model.getTagsMap()));
        modelInfo.setTagTrans(new HashMap<>(model.getTagTransMap()));

        modelInfo.setMaxFreq(maxFreq);

        logger.info("dic cnt : {}, inner cnt : {}, outer cnt : {}, tags cnt : {}, tagTrans cnt : {}",
                modelInfo.getDictionary().size(), modelInfo.getInner().size(), modelInfo.getOuter().size(), modelInfo.getTags().size(), modelInfo.getTagTrans().size());

        logger.info("max freq : {}", maxFreq );
        logger.info("model load elapsed : {} ms", watch.getTime() );

        return modelInfo;
    }

}

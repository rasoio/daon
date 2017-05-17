package daon.analysis.ko.model.loader;

import com.google.protobuf.CodedInputStream;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.fst.KeywordSeqFST;
import daon.analysis.ko.proto.Keyword;
import daon.analysis.ko.proto.Model;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

/**
 * Created by mac on 2017. 3. 8..
 */
public class ModelLoader {

    private Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);

    public static ModelLoader create() {
        return new ModelLoader();
    }

    private ModelLoader() {}

    private KeywordSeqFST fst;

    private Model model;


    public ModelLoader load() throws IOException {


        StopWatch watch = new StopWatch();

        watch.start();

//        CodedInputStream input = CodedInputStream.newInstance(new GZIPInputStream(new FileInputStream("/Users/mac/work/corpus/model/model.dat.gz")));
        CodedInputStream input = CodedInputStream.newInstance(new FileInputStream("/Users/mac/work/corpus/model/model.dat"));

        input.setSizeLimit(Integer.MAX_VALUE);

        model = Model.parseFrom(input);

        FST readFst = null;
        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
            PositiveIntOutputs.getSingleton(), // word weight
            IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        try (InputStream is = new ByteArrayInputStream(model.getFst().toByteArray())) {
            readFst = new FST<>(new InputStreamDataInput(new BufferedInputStream(is)), fstOutput);
        }

        fst = new KeywordSeqFST(readFst);


        Map<Integer, Keyword> dictionary = model.getDictionaryMap();

        watch.stop();

        logger.info("model load elapsed : {} ms", watch.getTime() );

        return this;
    }

    public KeywordSeqFST getFst() {
        return fst;
    }

    public Model getModel() {
        return model;
    }
}

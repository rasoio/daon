package daon.analysis.ko.model.builder;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.dict.BaseDictionary;
import daon.analysis.ko.dict.DictionaryBuilder;
import daon.analysis.ko.fst.KeywordFST;
import daon.analysis.ko.model.*;
import daon.analysis.ko.reader.JsonFileReader;
import daon.analysis.ko.util.CharTypeChecker;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.IntSequenceOutputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Created by mac on 2017. 3. 8..
 */
public class ModelBuilder {

    private Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);

    public static ModelBuilder create() {
        return new ModelBuilder();
    }

    private ModelBuilder() {}


    public void build() throws IOException {


        JsonFileReader reader = new JsonFileReader();

        StopWatch watch = new StopWatch();

        watch.start();

        List<Keyword> keywords = reader.read("/Users/mac/work/corpus/model/words.json", Keyword.class);
        List<IrrWord> irrWords = reader.read("/Users/mac/work/corpus/model/irr_words.json", IrrWord.class);

        List<InnerInfo> innerInfos = reader.read("/Users/mac/work/corpus/model/inner_info.json", InnerInfo.class);
        List<OuterInfo> outerInfos = reader.read("/Users/mac/work/corpus/model/outer_info.json", OuterInfo.class);

        List<KeywordSeq> keywordSeqs = new ArrayList<>();


    }


}

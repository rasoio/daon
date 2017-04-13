package daon.analysis.ko.dict;

import daon.analysis.ko.config.Config;
import daon.analysis.ko.fst.KeywordFST;
import daon.analysis.ko.reader.Reader;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.KeywordRef;
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

public class DictionaryBuilder {

    private Logger logger = LoggerFactory.getLogger(DictionaryBuilder.class);


    public Dictionary build() throws IOException {

//			logger.info("reader read complete");

        /**
         * 사전 전처리
         */
        StopWatch watch = new StopWatch();
        StopWatch totalWatch = new StopWatch();

        watch.start();
        totalWatch.start();

        //사전 목록
        List<KeywordRef> keywordRefs = new ArrayList<KeywordRef>();


        //전체 사전 정보
//            while (reader.hasNext()) {
//                Keyword keyword = reader.next();
//
//                //기본 사전 정보
//                KeywordRef ref = new KeywordRef(keyword);
//                keywordRefs.add(ref);
//            }

        watch.stop();

        logger.info("dictionary added : {} ms, size :{}", watch.getTime(), keywordRefs.size());

        watch.reset();
        watch.start();

        logger.info("fst pre load : {} ms, size :{}", watch.getTime(), keywordRefs.size());

        watch.reset();
        watch.start();

        Collections.sort(keywordRefs);

        watch.stop();

        logger.info("keywordRefs sorted : {} ms, size :{}", watch.getTime(), keywordRefs.size());

        watch.reset();
        watch.start();

        //seq 별 Keyword
        IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();
        Builder<IntsRef> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE4, fstOutput);

        Map<IntsRef, IntsRef> fstData = new LinkedHashMap<IntsRef, IntsRef>();
//			Map<IntsRef,IntsRef> fstData = new TreeMap<IntsRef,IntsRef>();

        //중복 제거, 정렬, output append
        for (int idx = 0, len = keywordRefs.size(); idx < len; idx++) {

            IntsRefBuilder curOutput = new IntsRefBuilder();

            KeywordRef keyword = keywordRefs.get(idx);

            if (keyword == null) {
                continue;
            }

//				logger.info("input : {}", keyword.getInput());

            final IntsRef input = keyword.getInput();

            IntsRef output = fstData.get(input);

            if (output != null) {
                curOutput.copyInts(output);
            }

            curOutput.append(idx);
            output = curOutput.get();

            //fst 추가, output 사용이 애매..
            fstData.put(input, output);

            keyword.clearInput();
        }

        watch.stop();

        logger.info("fstData load : {} ms, size :{}", watch.getTime(), fstData.size());

        watch.reset();
        watch.start();

        for (Map.Entry<IntsRef, IntsRef> e : fstData.entrySet()) {
            fstBuilder.add(e.getKey(), e.getValue());

//				logger.info("input : {} , output :{}", e.getKey(), e.getValue());
        }

//			fstData.clear();

        KeywordFST fst = new KeywordFST(fstBuilder.finish());

        watch.stop();

        logger.info("fst build : {} ms", watch.getTime());

        totalWatch.stop();

        logger.info("total : {} ms", totalWatch.getTime());

        return null;
//            Dictionary dictionary = new BaseDictionary(fst, keywordRefs, connectionCosts);

//            return dictionary;
    }
}

package daon.analysis.ko.model;

import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import daon.analysis.ko.DaonAnalyzer3;
import daon.analysis.ko.DaonAnalyzer4;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.KeywordSeqFST;
import daon.analysis.ko.model.loader.ModelLoader;
import daon.analysis.ko.proto.*;
import daon.analysis.ko.reader.JsonFileReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;

/**
 * Created by mac on 2017. 3. 8..
 */
public class TestMakeModel {

    private Logger logger = LoggerFactory.getLogger(TestMakeModel.class);

    private KeywordSeqFST fst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();

    private Map<ImmutablePair, Float> innerInfoMap;

    private Map<ImmutablePair, Float> outerInfoMap;

    private List<KeywordSeq> keywordSeqs;


    private Map<String, Float> tagsMap;
    private Map<ImmutablePair, Float> tagTransMap;


    private static int[] EMPTY_SEQ = new int[]{0};

    private float maxFreq;

//    @Before
    public void before() throws IOException {
        JsonFileReader reader = new JsonFileReader();

        StopWatch watch = new StopWatch();

        watch.start();

        List<Keyword> keywords = reader.read("/Users/mac/work/corpus/model/words.json", Keyword.class);
        List<IrrWord> irrWords = reader.read("/Users/mac/work/corpus/model/irr_words2.json", IrrWord.class);

        List<InnerInfo> innerInfos = reader.read("/Users/mac/work/corpus/model/inner_info2.json", InnerInfo.class);
        List<OuterInfo> outerInfos = reader.read("/Users/mac/work/corpus/model/outer_info2.json", OuterInfo.class);


        List<Tag> tags = reader.read("/Users/mac/work/corpus/model/tags.json", Tag.class);
        List<TagTran> tagTrans = reader.read("/Users/mac/work/corpus/model/tag_trans.json", TagTran.class);

        dictionary = keywords.stream().collect(Collectors.toMap(Keyword::getSeq, Function.identity()));

        keywordSeqs = new ArrayList<>();

        maxFreq = keywords.stream().mapToLong(Keyword::getTf).max().getAsLong();

        long maxInnerFreq = innerInfos.stream().mapToLong(InnerInfo::getCnt).max().getAsLong();

        keywords.forEach(keyword -> {
            String word = keyword.getWord();
            int seq = keyword.getSeq();
            long freq = keyword.getTf();

            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
            keywordSeq.setFreq(freq);

            keywordSeqs.add(keywordSeq);

        });

        //재정의 필요
        irrWords.forEach(irrWord -> {
            String word = irrWord.getSurface();
            int[] seq = irrWord.getWordSeqs();
            long freq = irrWord.getCnt();

//            String words = IntStream.of(seq).mapToObj(s->{
//                Keyword k = dictionary.get(s);
//                if(k == null) return "-";
//                return k.getWord() + "(" + k.getTag() + ")";
//            }).collect(Collectors.joining(", "));

            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
            keywordSeq.setFreq(freq);
            keywordSeqs.add(keywordSeq);
        });



        maxFreq = keywordSeqs.stream().mapToLong(KeywordSeq::getFreq).max().getAsLong();

//        innerInfos.forEach(inner -> {
//
//            int wordSeq = inner.getWordSeq();
//            int nextInnerSeq = inner.getnInnerSeq();
//
//
//            Keyword word = dictionary.get(wordSeq);
//            Keyword nextWord = dictionary.get(nextInnerSeq);
//
//            String sumWord = word.getWord() + nextWord.getWord();
//
//            KeywordSeq keywordSeq = new KeywordSeq(sumWord, wordSeq, nextInnerSeq);
//            keywordSeq.setFreq(inner.getCnt());
//
//            keywordSeqs.add(keywordSeq);
//
////            innerInfoMap.put(key, (inner.getCnt() / (float) maxInnerFreq));
//        });
//
//
//        List<String> line = new ArrayList<>();
//        //재정의 필요
//        irrWords.forEach(irrWord -> {
//            String word = irrWord.getSurface();
//            int[] seq = irrWord.getWordSeqs();
//            long freq = irrWord.getCnt();
//
//
//            String words = IntStream.of(seq).mapToObj(s->{
//                Keyword k = dictionary.get(s);
//                if(k == null) return "-";
//                return k.getWord() + "(" + k.getTag() + ")";
//            }).collect(Collectors.joining(", "));
//
//            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
//            keywordSeq.setFreq(freq);
//            keywordSeqs.add(keywordSeq);
//
//            line.add(word + " : " + words);
//        });

//        File file = new File("/Users/mac/work/corpus/model", "irr_text.txt");
//        FileUtils.writeLines(file, line);


        Collections.sort(keywordSeqs);

        //seq 별 Keyword

        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
            PositiveIntOutputs.getSingleton(), // word weight
            IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        Builder<Object> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE2, fstOutput);

        Map<IntsRef, IntsRef> fstData = new LinkedHashMap<IntsRef, IntsRef>();
//			Map<IntsRef,IntsRef> fstData = new TreeMap<IntsRef,IntsRef>();

        //중복 제거, 정렬, output append
        for (int idx = 0, len = keywordSeqs.size(); idx < len; idx++) {

            IntsRefBuilder curOutput = new IntsRefBuilder();

            KeywordSeq keyword = keywordSeqs.get(idx);

            if (keyword == null) {
                continue;
            }

            final IntsRef input = keyword.getInput();
            final int[] seqs = keyword.getSeqs();
            final long freq = keyword.getFreq();

            IntStream.of(seqs).forEach(curOutput::append);

            IntsRef wordSeqs = curOutput.get();

            PairOutputs.Pair<Long,IntsRef> pair = output.newPair(freq, wordSeqs);

            fstBuilder.add(input, pair);

            keyword.clearInput();
        }

        fst = new KeywordSeqFST(fstBuilder.finish());


//        long maxInnerFreq = innerInfos.stream().mapToLong(InnerInfo::getCnt).max().getAsLong();

        innerInfoMap = new HashMap<>(innerInfos.size());

        innerInfos.forEach(inner -> {

            ImmutablePair key = new ImmutablePair(inner.getWordSeq(), inner.getnInnerSeq());
            innerInfoMap.put(key, (inner.getCnt() / (float) maxInnerFreq));

        });

        long maxOuterFreq = outerInfos.stream().mapToLong(OuterInfo::getCnt).max().getAsLong();

        outerInfoMap = new HashMap<>(outerInfos.size());

        outerInfos.forEach(outer -> {
            ImmutablePair key = new ImmutablePair(outer.getpOuterSeq(), outer.getWordSeq());

            outerInfoMap.put(key, (outer.getCnt() / (float) maxOuterFreq));
        });


        long maxTagFreq = tags.stream().mapToLong(Tag::getCnt).max().getAsLong();

        tagsMap = tags.stream().collect(Collectors.toMap(Tag::getTag,
                v -> {
                    return (v.getCnt() / (float) maxTagFreq);
                }));


        long maxTagTransFreq = tagTrans.stream().mapToLong(TagTran::getCnt).max().getAsLong();

        tagTransMap = new HashMap<>(tagTrans.size());

        tagTrans.forEach(tagTran -> {
            ImmutablePair key = new ImmutablePair(tagTran.getTag(), tagTran.getnInnerTag());

            tagTransMap.put(key, (tagTran.getCnt() / (float) maxTagTransFreq ));
        });

        watch.stop();

        logger.info("total load : {} ms, size :{}", watch.getTime(), fstData.size());

        logger.info("dictionary added : {} ms, words size : {}, irr size : {}, inner size : {}, outer size : {}", watch.getTime(), keywordSeqs.size(), irrWords.size(), innerInfos.size(), outerInfos.size());

    }



    Path dictionaryPath = Paths.get("/Users/mac/work/corpus/model/dictionary.dat");
    Path fstPath = Paths.get("/Users/mac/work/corpus/model/fst.dat");

    @Test
    public void make() throws IOException, InterruptedException, ClassNotFoundException {


        before();

        save(dictionary, dictionaryPath);

        FST internalFST = fst.getInternalFST();

//        String fstStr = null;
        byte[] fstBytes = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            internalFST.save(new OutputStreamDataOutput(os));

//            fstStr = os.toString();
            fstBytes = os.toByteArray();

        }


        StopWatch watch = new StopWatch();

        watch.start();

        FST readFst = null;
        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
                PositiveIntOutputs.getSingleton(), // word weight
                IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        try (InputStream is = new ByteArrayInputStream(fstBytes)) {
            readFst = new FST<>(new InputStreamDataInput(new BufferedInputStream(is)), fstOutput);
        }

//        internalFST.save(fstPath);

        watch.stop();
        System.out.println(" elapsed : " + watch.getTime() );

//        한개 file 로 구성
//        dictionary, innerInfoMap, outerInfoMap, tagsMap, tagTransMap


    }

    @Test
    public void test() throws IOException, ClassNotFoundException {

        StopWatch watch = new StopWatch();

        watch.start();

        Map<Integer, Keyword> dictionary = (Map<Integer, Keyword>) load(dictionaryPath);

        System.out.println(dictionary.size());
        System.out.println(dictionary.get(204409));

        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
            PositiveIntOutputs.getSingleton(), // word weight
            IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        FST readFst = FST.read(fstPath, fstOutput);
        KeywordSeqFST readKeywordFst = new KeywordSeqFST(readFst);

//        DaonAnalyzer4 daonAnalyzer4 = new DaonAnalyzer4(readKeywordFst, dictionary);


//        String test = "아이폰 기다리다 지쳐 애플공홈에서 언락폰질러버렸다 6+ 128기가실버ㅋ";
//        String test = "1가A나다ABC라마바ABC";
//        String test = "사람이라는 느낌";
//        String test = "하늘을나는";
//        String test = "영광 굴비의 본디 이름은 정주 굴비다.";
//        String test = "우리나라는 대기 오염";
//        String test = "심쿵";
//        String test = "한나라당 조혜원님을";
//        String test = "도대체 어떻게 하라는거야?";
//        String test = "서울에서부산으로";

//        List<CandidateTerm> terms = daonAnalyzer4.analyze(test);

//        terms.forEach(System.out::println);

        watch.stop();
        System.out.println(" load + analyzed elapsed : " + watch.getTime() );


    }

    @Test
    public void writeModel() throws IOException {

        before();

        Model.Builder builder = Model.newBuilder();

        dictionary.values().forEach(keyword -> {
            daon.analysis.ko.proto.Keyword newKeyword = daon.analysis.ko.proto.Keyword.newBuilder()
                    .setSeq(keyword.getSeq())
                    .setWord(keyword.getWord())
                    .setTag(keyword.getTag().getName())
                    .setTf(keyword.getTf())
                    .build();


            builder.putDictionary(keyword.getSeq(), newKeyword);
        });

        innerInfoMap.entrySet().forEach(entry -> {
            String key = entry.getKey().getLeft() + "|" + entry.getKey().getRight();
            Float score = entry.getValue();

            builder.putInner(key.hashCode(), score);
        });


        outerInfoMap.entrySet().forEach(entry -> {
            String key = entry.getKey().getLeft() + "|" + entry.getKey().getRight();
            Float score = entry.getValue();

            builder.putOuter(key.hashCode(), score);
        });

        tagsMap.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Float score = entry.getValue();

            builder.putTags(key.hashCode(), score);
        });

        tagTransMap.entrySet().forEach(entry -> {
            String key = entry.getKey().getLeft() + "|" + entry.getKey().getRight();
            Float score = entry.getValue();

            builder.putTagTrans(key.hashCode(), score);
        });

        FST internalFST = fst.getInternalFST();

        byte[] fstBytes = null;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            internalFST.save(new OutputStreamDataOutput(os));

            fstBytes = os.toByteArray();
        }

        builder.setFst(ByteString.copyFrom(fstBytes));


        Model model = builder.build();


        System.out.println(model.getDictionaryCount());

        FileOutputStream output = new FileOutputStream("/Users/mac/work/corpus/model/model.dat");

        model.writeTo(output);

        output.close();


    }





    @Test
    public void readModel() throws IOException, InterruptedException, ClassNotFoundException {


        ModelLoader loader = ModelLoader.create().load();

        DaonAnalyzer4 daonAnalyzer4 = new DaonAnalyzer4(loader.getFst(), loader.getModel());

//        String test = "그가";
//        String test = "하늘을";
//        String test = "어디에 쓸 거냐거나 언제 갚을 수 있느냐거나 따위의, 돈을 빌려주는 사람이 으레 하게 마련인 질문 같은 것은 하지 않았다.";
        String test = "남자지갑";
//        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "사람이 으레 하게";
//        String test = "평화를 목숨처럼 여기는 우주 방위대 마인드C X 호조 작가의 최강 콜라보 파랗고 사랑스러운 녀석들이 매주 금요일 심쿵을 예고한다.";
//        String test = "하지만 질투하는 마음도 있거든요.";
//        String test = "보여지므로";
//        String test = "선생님은 쟝의 소식을 모른다고 하지만 저는 그렇게 생각하지 않아요.";
//        String test = "아버지가방에들어가신다";
//        String test = "아이폰 기다리다 지쳐 애플공홈에서 언락폰질러버렸다 6+ 128기가실버ㅋ";
//        String test = "1가A나다ABC라마바ABC";
//        String test = "사람이라는 느낌";
//        String test = "하늘을나는";
//        String test = "영광 굴비의 본디 이름은 정주 굴비다.";
//        String test = "우리나라는 대기 오염";
//        String test = "심쿵";
//        String test = "한나라당 조혜원님을";
//        String test = "도대체 어떻게 하라는거야?";
//        String test = "서울에서부산으로";

        List<CandidateTerm> terms = daonAnalyzer4.analyze(test);
        List<EojeolInfo> eojeolInfos = daonAnalyzer4.analyzeText(test);

        logger.info("###################################");

        eojeolInfos.forEach(System.out::println);

//        Thread.sleep(1000000);

    }


    private void save(Object obj, Path path) throws IOException {
        try (ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(path))) {
            os.writeObject(obj);
        }
    }



    private Object load(Path path) throws ClassNotFoundException, IOException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
            return in.readObject();
        }
    }
}

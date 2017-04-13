package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.KeywordSeqFST;
import daon.analysis.ko.reader.JsonFileReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.apache.lucene.util.fst.PairOutputs.Pair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mac on 2017. 3. 8..
 */
public class TestModel2 {

    private Logger logger = LoggerFactory.getLogger(TestModel2.class);

    private KeywordSeqFST fst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();

    private Map<ImmutablePair, Long> innerInfoMap;

    private Map<ImmutablePair, Long> outerInfoMap;

    private List<KeywordSeq> keywordSeqs;


    private Map<String, Long> tagsMap;
    private Map<ImmutablePair, Long> tagTransMap;

    private FST<IntsRef> fst2;


    @Before
    public void before() throws IOException {
        JsonFileReader reader = new JsonFileReader();

        StopWatch watch = new StopWatch();

        watch.start();

        List<Keyword> keywords = reader.read("/Users/mac/work/corpus/model/words.json", Keyword.class);
        List<IrrWord> irrWords = reader.read("/Users/mac/work/corpus/model/irr_words.json", IrrWord.class);

        List<InnerInfo> innerInfos = reader.read("/Users/mac/work/corpus/model/inner_info2.json", InnerInfo.class);
        List<OuterInfo> outerInfos = reader.read("/Users/mac/work/corpus/model/outer_info2.json", OuterInfo.class);


        List<Tag> tags = reader.read("/Users/mac/work/corpus/model/tags.json", Tag.class);
        List<TagTran> tagTrans = reader.read("/Users/mac/work/corpus/model/tag_trans.json", TagTran.class);

        keywordSeqs = new ArrayList<>();

        keywords.forEach(keyword -> {
            String word = keyword.getWord();
            int seq = keyword.getSeq();
            long freq = keyword.getTf();

            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
            keywordSeq.setWeight(freq);

            keywordSeqs.add(keywordSeq);

        });

//        irrWords.forEach(irrWord -> {
//            String word = irrWord.getSurface();
//            int[] seq = irrWord.getWordSeqs();
//
//            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
//            keywordSeqs.add(keywordSeq);
//        });

        Collections.sort(keywordSeqs);

        //seq 별 Keyword

        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
            PositiveIntOutputs.getSingleton(), // word weight
            IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

        Builder<Object> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE4, fstOutput);

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
            final long weight = keyword.getWeight();

            IntStream.of(seqs).forEach(curOutput::append);

            IntsRef wordSeqs = curOutput.get();


            Pair<Long,IntsRef> pair = output.newPair(weight, wordSeqs);


            fstBuilder.add(input, pair);

            keyword.clearInput();
        }

        fst = new KeywordSeqFST(fstBuilder.finish());

        // ㅜㅜ
        dictionary = keywords.stream().collect(Collectors.toMap(Keyword::getSeq, Function.identity()));
//        keywords.stream().forEach(
//            k -> {
//                dictionary.put(k.getSeq(), k);
//            }
//        );

        innerInfoMap = new HashMap<>(innerInfos.size());

        innerInfos.forEach(inner -> {

            ImmutablePair key = new ImmutablePair(inner.getWordSeq(), inner.getnInnerSeq());
            innerInfoMap.put(key, inner.getCnt());

        });


        outerInfoMap = new HashMap<>(outerInfos.size());

        outerInfos.forEach(outer -> {
            ImmutablePair key = new ImmutablePair(outer.getpOuterSeq(), outer.getWordSeq());

            outerInfoMap.put(key, outer.getCnt());
        });


        tagsMap = tags.stream().collect(Collectors.toMap(Tag::getTag, Tag::getCnt));


        tagTransMap = new HashMap<>(tagTrans.size());

        tagTrans.forEach(tagTran -> {
            ImmutablePair key = new ImmutablePair(tagTran.getTag(), tagTran.getnInnerTag());

            tagTransMap.put(key, tagTran.getCnt());
        });

//        innerInfoMap = innerInfos.stream().collect(Collectors.groupingBy(InnerInfo::getWordSeq));
//        outerInfoMap = outerInfos.stream().collect(Collectors.groupingBy(OuterInfo::getpOuterSeq));


        watch.stop();

        logger.info("total load : {} ms, size :{}", watch.getTime(), fstData.size());

        logger.info("dictionary added : {} ms, words size : {}, irr size : {}, inner size : {}, outer size : {}", watch.getTime(), keywords.size(), irrWords.size(), innerInfos.size(), outerInfos.size());

    }


    @Test
    public void test1() throws IOException, InterruptedException {
        List<Term> terms = read();

        terms.forEach(System.out::println);
    }

    public List<Term> read() throws IOException, InterruptedException {

        List<Term> terms = new ArrayList<>();


//        String test = "그가";
//        String test = "하늘을";
//        String test = "어디에 쓸 거냐거나 언제 갚을 수 있느냐거나 따위의, 돈을 빌려주는 사람이 으레 하게 마련인 질문 같은 것은 하지 않았다.";
//        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "사람이 으레 하게";
//        String test = "평화를 목숨처럼 여기는 우주 방위대 마인드C X 호조 작가의 최강 콜라보 파랗고 사랑스러운 녀석들이 매주 금요일 심쿵을 예고한다.";
//        String test = "하지만 질투하는 마음도 있거든요.";
//        String test = "보여지므로";
//        String test = "선생님은 쟝의 소식을 모른다고 하지만 저는 그렇게 생각하지 않아요.";
//        String test = "아버지가방에들어가신다";
//        String test = "1가A나다ABC라마바ABC";
        String test = "사람이라는 느낌";
//        String test = "하늘을나는";
//        String test = "심쿵";
//        String test = "한나라당 조혜원님을";
//        String test = "도대체 어떻게 하라는거야?";

        String[] eojeols = test.split("\\s");

        int outerPrevSeq = 0;

        //tokenizer results
        for(String eojeol : eojeols) {

            TreeMap<Integer, Term> results = new TreeMap<>();

            char[] eojeolChars = eojeol.toCharArray();
            int eojeolLength = eojeolChars.length;

            //사전 탐색 결과
            TreeMap<Integer, List<Word>> eojeolResults = lookup(eojeolChars, eojeolLength);

            //전체 어절 - 사전 참조 된 영역 = 누락 된 영역 추출
            findUnknownWords(eojeolChars, eojeolLength, eojeolResults, results);

            //connection 찾기
            findConnection(outerPrevSeq, eojeolLength, eojeolResults, results);

            results.entrySet().forEach(e->{
                terms.add(e.getValue());
            });

            Map.Entry<Integer, Term> e = results.lastEntry();

            //last word seq
            if(e != null) {
                outerPrevSeq = e.getValue().getKeyword().getSeq();
            }

        }

//        try {
//            Thread.sleep(1000000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        return terms;
    }

    private void findUnknownWords(char[] eojeolChars, int eojeolLength, TreeMap<Integer, List<Word>> eojeolResults, Map<Integer, Term> results) {

        for(int i =0; i< eojeolLength; i++) {

            List<Word> words = eojeolResults.get(i);
//                logger.info("==> check idx : {}", i);

            if (words == null) {

                int offset = i;
                //다음 사전 단어의 위치
                int nextIdx = findNextIdx(offset, eojeolLength, eojeolResults);
                int length = (nextIdx - offset);
                String word = new String(eojeolChars, offset, length);
                //add unknown word
                logger.info("unkown starIdx : {}, length : {}, str : {}", offset, length, word);

                results.put(offset, new Term(new Keyword(word, POSTag.NNG), offset, length));

                i = nextIdx;
            }else{
                //마지막 word (가장 긴 단어)
                Word lastWord = words.get(words.size()-1);
                int length = lastWord.getLength();

                //다음 idx로
                i = i + (length - 1);
            }
        }
    }

    private int findNextIdx(int offset, int eojeolLength, TreeMap<Integer, List<Word>> eojeolResults) {

        int idx = offset + 1;

        for(; idx < eojeolLength; idx++) {

            List<Word> words = eojeolResults.get(idx);

            if(words != null){
                break;
            }
        }

        return idx;
    }


    /**
     * 개선 필요...
     * @param eojeolLength
     * @param eojeolResults
     * @param results
     */
    private void findConnection(int outerPrevSeq, int eojeolLength, TreeMap<Integer, List<Word>> eojeolResults, Map<Integer, Term> results) {

        //recurrent i => 인자
        for(int i=0; i< eojeolLength; i++) {

            List<Word> ref = eojeolResults.get(i);

            int innerPrevSeq = -1;

            if (ref != null) {

                TreeSet<CandidateResults> queue = new TreeSet<>(scoreComparator);


//                if(i==0) {
                    //output connection 찾기.. 의미 없을수도.. 확률이 높은 경우만 사용할지...??
//                    findOuterConnection(outerPrevSeq, ref, eojeolResults);
//                }

                logger.info("find connection idx : {}", i);

//                i = findInnerConnection(i, ref, eojeolResults, results, -1);

//                logger.info("results : {}", results);


                find(queue, i, ref, eojeolResults);


                for(CandidateResults result : queue){

                    logger.info("result : {}", result);
                }


                CandidateResults first = queue.first();


//                results.put(offset, new Term(word, offset, word.getLength()));

            }




            // 최대 스코어 사용
            // 우선순위 (최장일치)
            // 1. 표층형이 가장 길게 연결된 결과
            // 2. 표층형이 가장 길게 매칭된 사전 결과

            // 같은 길이 일때
            // 최대 스코어 값 사용
            // 연결 매칭 점수 - 연결 빈도, 태그 결합 빈도 (누적 점수)
            // 단일 사전 점수 - 노출 빈도, 단일 태그 노출 빈도
        }




//            0 idx 에서 시작
//
//            //다음 단어 기분석 사전 결과 있으면 -> 조합 -> 그 다음 기 분석 사전 결과 조회 있으면 -> 조합
//            List nextWords = getNextWord(nextIdx)
//
//            Result result = calcuate(curList, nextWords)
//
//            //recursive function 내용
//            if(result.find){
//                List nextWords = getNextWord(result.nextIdx)
//
//                Result result = calcuate(result.findWords, nextWords)
//
//                // doRecursive
////                        if(result.find){
////                            List nextWords = getNextWord(result.nextIdx)
////
////                            Result result = calcuate(result.findWords, nextWords)
////
////                            if(result.find) {
////                                List nextWords = getNextWord(result.nextIdx)
////
////                                Result result = calcuate(result.findWords, nextWords)
////
////                                if (result.find) {
////                                    List nextWords = getNextWord(result.nextIdx)
////
////                                    Result result = calcuate(result.findWords, nextWords)
////                                }
////                            }
////                        }
//            }else{
//                // 최종 매칭 결과
//                return (finalMatchWord, matchedWords(Map<Integer(offset), Term(keyword)> results), score)
//            }
    }

    private void find(TreeSet<CandidateResults> queue, int offset, List<Word> ref, TreeMap<Integer, List<Word>> eojeolResults) {

        for (Word w : ref) {

            int seq = w.getLastSeq();
            int length = w.getLength();

            final int nextOffset = offset + length;
            List<Word> nref = eojeolResults.get(nextOffset);

            boolean match = false;

            //연결 확인이 가능할 경우
            if (nref != null) {

                for (Word nw : nref) {

                    int nextSeq = nw.getHeadSeq();
                    int nextLength = nw.getLength();

                    Long cnt = findInnerSeq(seq, nextSeq);

                    if(cnt != null){


                        CandidateResults candidateResults = new CandidateResults();

                        List<Keyword> keywords = getKeywords(w);

                        keywords.addAll(getKeywords(nw));

                        ExplainInfo explainInfo = new ExplainInfo();
                        explainInfo.setMatchType(explainInfo.newMatchType("CONNECTION", ArrayUtils.addAll(w.getSeq(),nw.getSeq())));
                        explainInfo.setFreqScore(cnt);
//                        explainInfo.setTagScore(w.getFreq());

                        candidateResults.add(keywords, explainInfo);

                        match = true;

                        queue.add(candidateResults);

                        final int findOffset = nextOffset + nextLength;

//                        logger.info("find deep start!! offset : {},  result : {}", findOffset, candidateResults);

                        findDeep(queue, findOffset, candidateResults, nw, eojeolResults);


                    }

                }

            }

            if(match == false){

                CandidateResults candidateResults = new CandidateResults();


                List<Keyword> keywords = getKeywords(w);

                ExplainInfo explainInfo = new ExplainInfo();
                explainInfo.setMatchType(explainInfo.newMatchType("DICTIONARY", w.getSeq()));
                explainInfo.setFreqScore(w.getFreq());
//                explainInfo.setTagScore(w.getFreq());


                candidateResults.add(keywords, explainInfo);


                queue.add(candidateResults);
            }
        }

    }

    private void findDeep(TreeSet<CandidateResults> queue, int findOffset, CandidateResults candidateResults, Word word, TreeMap<Integer, List<Word>> eojeolResults) {

        List<Word> nextRef = eojeolResults.get(findOffset);

        if(nextRef != null){

            int seq = word.getLastSeq();

            for (Word nw : nextRef) {
                int nextSeq = nw.getHeadSeq();
                int nextLength = nw.getLength();

                Long cnt = findInnerSeq(seq, nextSeq);


//                logger.info("loop seq : {},  nextSeq : {}, results : {}", seq, nextSeq, candidateResults);

                if(cnt != null){

                    CandidateResults newcandidateResults = candidateResults.clone();
//                    newcandidateResults.setKeywords(candidateResults.getKeywords());
//                    newcandidateResults.setExplainInfos(candidateResults.getExplainInfos());
//                    newcandidateResults.calculateScore();

//                    CandidateResults clonedCandidateResults = candidateResults.clone();
                    List<Keyword> keywords = getKeywords(nw);

                    ExplainInfo explainInfo = new ExplainInfo();
                    explainInfo.setMatchType(explainInfo.newMatchType("CONNECTION", ArrayUtils.addAll(word.getSeq(),nw.getSeq())));
                    explainInfo.setFreqScore(cnt);
//                        explainInfo.setTagScore(w.getFreq());

                    newcandidateResults.add(keywords, explainInfo);


                    final int deepFindOffset = findOffset + nextLength;

//                    logger.info("find deep offset : {},  result : {}", deepFindOffset, candidateResults);

                    findDeep(queue, deepFindOffset, newcandidateResults, nw, eojeolResults);

                    queue.add(newcandidateResults);

//                    return newcandidateResults;
                }



            }

        }

    }

    private List<Keyword> getKeywords(Word w) {
        return IntStream.of(w.getSeq()).mapToObj(i->{

            Keyword keyword = dictionary.get(i);

            return keyword;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    static final Comparator<CandidateResults> scoreComparator = (left, right) -> {

        return left.getScore() > right.getScore() ? -1 : 1;
    };


    private void findOuterConnection(int outerPrevSeq, List<Word> ref, TreeMap<Integer, List<Word>> eojeolResults) {
        if (outerPrevSeq > 0) {
            for (Word w : ref) {
                int seq = w.getHeadSeq();

                Long cnt = findOuterSeq(outerPrevSeq, seq);

                if (cnt != null) {
                    Keyword keyword = dictionary.get(seq);
                    Keyword prevKeyword = dictionary.get(outerPrevSeq);

                    logger.info("find outer ==> prev keyword : {}, cur keyword : {}, cnt : {}", prevKeyword, keyword, cnt);
                }
            }
        }
    }




    /**
     * 첫번째 문자열에 대해서 find
     *
     * 최종 결과 리턴 (최대 매칭 결과)
     * @param chars
     * @param charsLength
     * @return
     * @throws IOException
     */
    private TreeMap<Integer, List<Word>> lookup(char[] chars, int charsLength) throws IOException {

        //offset 별 기분석 사전 Term 추출 결과
        TreeMap<Integer, List<Word>> results = new TreeMap<>();

        final FST.BytesReader fstReader = fst.getBytesReader();

        FST.Arc<Object> arc = new FST.Arc<>();

        int offset = 0;

        for (; offset < charsLength; offset++) {
            arc = fst.getFirstArc(arc);
            Object output = fst.getOutputs().getNoOutput();
            int remaining = charsLength - offset;

            for (int i = 0; i < remaining; i++) {
                int ch = chars[offset + i];

//                CharType charType = CharTypeChecker.charType(ch);

                //탐색 결과 없을때
                if (fst.findTargetArc(ch, arc, arc, i == 0, fstReader) == null) {
                    break; // continue to next position
                }

                //탐색 결과는 있지만 종료가 안되는 경우 == prefix 만 매핑된 경우
                output = fst.getOutputs().add(output, arc.output);

                // 매핑 종료
                if (arc.isFinal()) {

                    //사전 매핑 정보 output
                    final Object outputs = fst.getOutputs().add(output, arc.nextFinalOutput);


                    PairOutputs<Long,IntsRef> _output = new PairOutputs<>(
                            PositiveIntOutputs.getSingleton(), // word weight
                            IntSequenceOutputs.getSingleton()  // connection wordId's
                    );

                    ListOfOutputs<Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(_output);

                    List<Pair<Long,IntsRef>> list = fstOutput.asList(outputs);

//                    logger.info("list : {}", list);

//                    List<Pair<Long,IntsRef>> outputList = output.asList(output);


                    //표층형 단어
                    final String word = new String(chars, offset, (i + 1));

                    final int length = (i + 1);

                    //위치에 따라 불가능한 형태소는 제거 필요

                    List sb = new ArrayList();

                    for(Pair<Long,IntsRef> pair : list){

                        IntStream.of(pair.output2.ints).forEach(seq ->{

                            Keyword k = dictionary.get(seq);

                            sb.add(k);
                        });


//                        logger.info("pair output1 : {}, output2 : {}", pair.output1, pair.output2);
                    }

                    logger.info(" {} ({}) : {}", word, list.size(), sb);

                    addResults(results, offset, length, list);

                }
            }

            logger.info("next word =========>");
        }

        return results;
    }

    class Word {
        private int[] seq;
        private int length;

        private int headSeq;
        private int lastSeq;

        private long freq;

        public Word(int[] seq, int length, long freq) {
            this.seq = seq;
            this.length = length;
            this.freq = freq;

            if(seq.length > 0) {
                this.headSeq = seq[0];
                this.lastSeq = seq[seq.length - 1];
            }

        }

        public int getHeadSeq() {
            return headSeq;
        }

        public int getLastSeq() {
            return lastSeq;
        }

        public int[] getSeq() {
            return seq;
        }

        public void setSeq(int[] seq) {
            this.seq = seq;
        }

        public long getFreq() {
            return freq;
        }

        public void setFreq(long freq) {
            this.freq = freq;
        }

        private List<Keyword> getKeywords() {
            return IntStream.of(seq).mapToObj(i->{

                Keyword keyword = dictionary.get(i);

                return keyword;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        @Override
        public String toString() {
            return "{" +
                    "seq=" + seq +
                    ", keyword=" + dictionary.get(seq) +
                    ", length=" + length +
                    '}';
        }
    }

    /**
     * 결과에 키워드 term 추가
     * @param results
     * @param offset
     * @param length
     * @param list
     */
    private void addResults(Map<Integer, List<Word>> results, int offset, int length, List<Pair<Long,IntsRef>> list) {
        List<Word> words = results.get(offset);

        if(words == null){
            words = new ArrayList<>();
        }

        for(Pair<Long,IntsRef> pair : list){

            words.add(new Word(pair.output2.ints, length, pair.output1));

        }

        results.put(offset, words);
    }


    private Long findOuterSeq(int outerPrevSeq, int seq){

        ImmutablePair key = new ImmutablePair(outerPrevSeq, seq);

        Long cnt = outerInfoMap.get(key);

        return cnt;
    }

    private Long findInnerSeq(int seq, int nSeq){

        ImmutablePair key = new ImmutablePair(seq, nSeq);

        Long cnt = innerInfoMap.get(key);

        return cnt;
    }
}

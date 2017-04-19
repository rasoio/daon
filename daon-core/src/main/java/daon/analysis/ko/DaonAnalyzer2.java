package daon.analysis.ko;

import daon.analysis.ko.config.CharType;
import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.dict.Dictionary;
import daon.analysis.ko.fst.KeywordSeqFST;
import daon.analysis.ko.model.*;
import daon.analysis.ko.reader.JsonFileReader;
import daon.analysis.ko.util.CharTypeChecker;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.analysis.util.RollingCharBuffer;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DaonAnalyzer2 {

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer2.class);

    private KeywordSeqFST fst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();

    private Map<ImmutablePair, Float> innerInfoMap;

    private Map<ImmutablePair, Float> outerInfoMap;

    private List<KeywordSeq> keywordSeqs;


    private Map<String, Float> tagsMap;
    private Map<ImmutablePair, Float> tagTransMap;


    private static int[] EMPTY_SEQ = new int[]{0};

    private float maxFreq;

    public DaonAnalyzer2() throws IOException {
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


        maxFreq = keywords.stream().mapToLong(Keyword::getTf).max().getAsLong();

        keywords.forEach(keyword -> {
            String word = keyword.getWord();
            int seq = keyword.getSeq();
            long freq = keyword.getTf();

            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
            keywordSeq.setFreq(freq);

            keywordSeqs.add(keywordSeq);

        });

        //재정의 필요
//        irrWords.forEach(irrWord -> {
//            String word = irrWord.getSurface();
//            int[] seq = irrWord.getWordSeqs();
//            long freq = irrWord.getCnt();
//
//            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
//            keywordSeq.setFreq(freq);
//            keywordSeqs.add(keywordSeq);
//        });

        Collections.sort(keywordSeqs);

        //seq 별 Keyword

        PairOutputs<Long,IntsRef> output = new PairOutputs<>(
                PositiveIntOutputs.getSingleton(), // word weight
                IntSequenceOutputs.getSingleton()  // connection wordId's
        );

        ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(output);

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
            final long freq = keyword.getFreq();

            IntStream.of(seqs).forEach(curOutput::append);

            IntsRef wordSeqs = curOutput.get();

            PairOutputs.Pair<Long,IntsRef> pair = output.newPair(freq, wordSeqs);

            fstBuilder.add(input, pair);

            keyword.clearInput();
        }

        fst = new KeywordSeqFST(fstBuilder.finish());

        // ㅜㅜ
        dictionary = keywords.stream().collect(Collectors.toMap(Keyword::getSeq, Function.identity()));

        long maxInnerFreq = innerInfos.stream().mapToLong(InnerInfo::getCnt).max().getAsLong();

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

        logger.info("dictionary added : {} ms, words size : {}, irr size : {}, inner size : {}, outer size : {}", watch.getTime(), keywords.size(), irrWords.size(), innerInfos.size(), outerInfos.size());


    }


    public List<CandidateTerm> analyze(String text) throws IOException {

        List<CandidateTerm> terms = new ArrayList<>();

        String[] eojeols = text.split("\\s");

        int outerPrevSeq = 0;

        //tokenizer results
        for(String eojeol : eojeols) {

            TreeMap<Integer, CandidateTerm> results = new TreeMap<>();

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

            Map.Entry<Integer, CandidateTerm> e = results.lastEntry();

            //last word seq
            if(e != null) {
                outerPrevSeq = e.getValue().getLastSeq();
            }

        }

        return terms;
    }

    private void findUnknownWords(char[] eojeolChars, int eojeolLength, TreeMap<Integer, List<Word>> eojeolResults, Map<Integer, CandidateTerm> results) {

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

                Keyword keyword = new Keyword(word, POSTag.NNG);
                List<Keyword> keywords = new ArrayList<>(1);
                keywords.add(keyword);

                ExplainInfo explainInfo = new ExplainInfo();
                explainInfo.setMatchType(explainInfo.newMatchType("UNKNOWN", EMPTY_SEQ ));

                CandidateTerm term = new CandidateTerm(offset, length, word, keywords, explainInfo);

                results.put(offset, term);
//                results.put(offset, new Term(new Keyword(word, POSTag.NNG), offset, length));

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
    private void findConnection(int outerPrevSeq, int eojeolLength, TreeMap<Integer, List<Word>> eojeolResults, TreeMap<Integer, CandidateTerm> results) {

        //recurrent i => 인자
        for(int i=0; i< eojeolLength; i++) {

            List<Word> ref = eojeolResults.get(i);

            //분석 결과가 있는 경우
            if (ref != null) {

                int prevSeq = -1;

                if(i == 0){
                    prevSeq = outerPrevSeq;
                }else{
                    //i > last result's last seq
                    CandidateTerm t = results.lowerEntry(i).getValue();

                    prevSeq = t.getLastSeq();
                }

                TreeSet<CandidateResult> queue = new TreeSet<>(scoreComparator);

                logger.info("find connection idx : {}", i);

                find(prevSeq, queue, i, ref, eojeolResults);

                CandidateResult first = queue.first();

                queue.stream().limit(5).forEach(r -> logger.info("result : {}", r));

                i += (first.getLength() - 1);

                for(CandidateTerm term : first.getTerms()){

                    results.put(term.getOffset(), term);

                }
            }

        }

    }

    private void find(int prevSeq, TreeSet<CandidateResult> queue, int offset, List<Word> ref, TreeMap<Integer, List<Word>> eojeolResults) {

        for (Word w : ref) {

            int seq = w.getLastSeq();
            int length = w.getLength();

            CandidateResult candidateResult = new CandidateResult();

            List<Keyword> keywords = w.getKeywords();

            ExplainInfo explainInfo = new ExplainInfo();

            if(prevSeq > 0){

                Float cnt = null;
                if(offset == 0){
                    cnt = findOuterSeq(prevSeq, seq);
                }else{
                    cnt = findInnerSeq(prevSeq, seq);
                }

                if(cnt != null) {

                    //TODO 기존 dictionary 스코어도 가져가야함. 합산
                    explainInfo.setMatchType(explainInfo.newMatchType("PREV_CONNECT", w.getSeq()));
                    explainInfo.setFreqScore(cnt);
                    explainInfo.setTagScore(getTagScore(prevSeq, seq));

                }else{

                    explainInfo.setMatchType(explainInfo.newMatchType("DICTIONARY", w.getSeq()));
                    explainInfo.setFreqScore(w.getFreq());
                    explainInfo.setTagScore(getTagScore(seq));
                }
            }else{

                explainInfo.setMatchType(explainInfo.newMatchType("DICTIONARY", w.getSeq()));
                explainInfo.setFreqScore(w.getFreq());
                explainInfo.setTagScore(getTagScore(seq));

            }


            CandidateTerm term = new CandidateTerm(offset, length, w.getSurface(), keywords, explainInfo);

            candidateResult.add(term);

            queue.add(candidateResult);

            final int nextOffset = offset + length;
            List<Word> nref = eojeolResults.get(nextOffset);

            //연결 확인이 가능할 경우
            if (nref != null) {

                for (Word nw : nref) {

                    int nextSeq = nw.getHeadSeq();
                    int nextLength = nw.getLength();

                    Float cnt = findInnerSeq(seq, nextSeq);

                    if(cnt != null){

                        CandidateResult newcandidateResult = candidateResult.clone();

                        List<Keyword> nextKeywords = nw.getKeywords();

                        ExplainInfo nextExplainInfo = new ExplainInfo();
                        nextExplainInfo.setMatchType(explainInfo.newMatchType("NEXT_CONNECTION", ArrayUtils.addAll(w.getSeq(),nw.getSeq())));
                        nextExplainInfo.setFreqScore(cnt);
                        nextExplainInfo.setTagScore(getTagScore(seq, nextSeq));

                        CandidateTerm nextTerm = new CandidateTerm(nextOffset, nextLength, nw.getSurface(), nextKeywords, nextExplainInfo);

                        newcandidateResult.add(nextTerm);

//                    logger.info("find deep offset : {},  result : {}", deepFindOffset, candidateResult);
                        queue.add(newcandidateResult);

                        final int findOffset = nextOffset + nextLength;

//                        logger.info("find deep start!! offset : {},  result : {}", findOffset, candidateResult);

                        findDeep(queue, findOffset, newcandidateResult, nw, eojeolResults);

                    }

                }

            }

        }

    }

    private void findDeep(TreeSet<CandidateResult> queue, int findOffset, CandidateResult candidateResult, Word word, TreeMap<Integer, List<Word>> eojeolResults) {

        List<Word> nextRef = eojeolResults.get(findOffset);

        if(nextRef != null){

            int seq = word.getLastSeq();

            for (Word nw : nextRef) {
                int nextSeq = nw.getHeadSeq();
                int nextLength = nw.getLength();

                Float cnt = findInnerSeq(seq, nextSeq);

//                logger.info("loop seq : {},  nextSeq : {}, results : {}", seq, nextSeq, candidateResult);

                if(cnt != null){

                    CandidateResult newcandidateResult = candidateResult.clone();

                    List<Keyword> keywords = nw.getKeywords();

                    ExplainInfo explainInfo = new ExplainInfo();
                    explainInfo.setMatchType(explainInfo.newMatchType("NEXT_CONNECTION", ArrayUtils.addAll(word.getSeq(),nw.getSeq())));
                    explainInfo.setFreqScore(cnt);
                    explainInfo.setTagScore(getTagScore(seq, nextSeq));

                    CandidateTerm term = new CandidateTerm(findOffset, nextLength, nw.getSurface(), keywords, explainInfo);

                    newcandidateResult.add(term);

                    final int deepFindOffset = findOffset + nextLength;

//                    logger.info("find deep offset : {},  result : {}", deepFindOffset, candidateResult);
                    queue.add(newcandidateResult);

                    findDeep(queue, deepFindOffset, newcandidateResult, nw, eojeolResults);

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

    /**
     * 최대 스코어 사용
     * 우선순위 (최장일치)
     * 1. 표층형이 가장 길게 연결된 결과
     * 2. 표층형이 가장 길게 매칭된 사전 결과

     * 같은 길이 일때
     * 최대 스코어 값 사용
     * 연결 매칭 점수 - 연결 빈도, 태그 결합 빈도 (누적 점수)
     * 단일 사전 점수 - 노출 빈도, 단일 태그 노출 빈도
     */
    static final Comparator<CandidateResult> scoreComparator = (left, right) -> {

        //최장일치 우선
        if(left.getLength() > right.getLength()){
            return -1;
        }else{

            //같은 길이인 경우 높은 스코어 우선
            if(left.getLength() == right.getLength()){
                return left.getScore() > right.getScore() ? -1 : 1;
            }else{
                return 1;
            }
        }
    };


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

                    ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(_output);

                    List<PairOutputs.Pair<Long,IntsRef>> list = fstOutput.asList(outputs);

//                    logger.info("list : {}", list);

//                    List<Pair<Long,IntsRef>> outputList = output.asList(output);


                    //표층형 단어
                    final String word = new String(chars, offset, (i + 1));

                    final int length = (i + 1);

                    //위치에 따라 불가능한 형태소는 제거 필요

                    //디버깅용 로깅
                    List sb = new ArrayList();

                    for(PairOutputs.Pair<Long,IntsRef> pair : list){

                        IntStream.of(pair.output2.ints).forEach(seq ->{

                            Keyword k = dictionary.get(seq);

                            sb.add(k);
                        });

//                        logger.info("pair output1 : {}, output2 : {}", pair.output1, pair.output2);
                    }

                    logger.info(" {} ({}) : {}", word, list.size(), sb);

                    addResults(results, offset, length, word, list);

                }
            }

            logger.info("next word =========>");
        }

        return results;
    }

    class Word {
        private int[] seq;

        private String surface;
        private int offset;
        private int length;

        private int headSeq;
        private int lastSeq;

        private float freq;

        public Word(int[] seq, int offset, int length, String surface, long freq) {
            this.seq = seq;
            this.offset = offset;
            this.length = length;
            this.surface = surface;
            this.freq = (freq / maxFreq);

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

        public float getFreq() {
            return freq;
        }

        public void setFreq(float freq) {
            this.freq = freq;
        }

        public String getSurface() {
            return surface;
        }

        public int getOffset() {
            return offset;
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
    private void addResults(Map<Integer, List<Word>> results, int offset, int length, String surface, List<PairOutputs.Pair<Long,IntsRef>> list) {
        List<Word> words = results.get(offset);

        if(words == null){
            words = new ArrayList<>();
        }

        for(PairOutputs.Pair<Long,IntsRef> pair : list){
            words.add(new Word(pair.output2.ints, offset, length, surface, pair.output1));
        }

        results.put(offset, words);
    }


    private Float findOuterSeq(int outerPrevSeq, int seq){

        ImmutablePair key = new ImmutablePair(outerPrevSeq, seq);

        Float cnt = outerInfoMap.get(key);

        return cnt;
    }

    private Float findInnerSeq(int seq, int nSeq){

        ImmutablePair key = new ImmutablePair(seq, nSeq);

        Float cnt = innerInfoMap.get(key);

        return cnt;
    }


    private float getTagScore(int seq, int nSeq){

        float score = 0;

        Keyword keyword = dictionary.get(seq);
        Keyword nKeyword = dictionary.get(nSeq);

        if(keyword != null && nKeyword != null){
            String tag = keyword.getTag().name();
            String nTag = nKeyword.getTag().name();

            ImmutablePair key = new ImmutablePair(tag, nTag);

            Float freq = tagTransMap.get(key);

            if(freq != null) {
                score = freq;
            }
        }

        return score;
    }


    private float getTagScore(int seq){

        float score = 0;

        Keyword keyword = dictionary.get(seq);

        if(keyword != null){
            String key = keyword.getTag().name();

            Float freq = tagsMap.get(key);

            if(freq != null) {
                score = freq;
            }
        }

        return score;
    }

}

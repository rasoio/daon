package daon.analysis.ko;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.KeywordSeqFST;
import daon.analysis.ko.model.*;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.proto.*;
import daon.analysis.ko.reader.JsonFileReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DaonAnalyzer4 implements Serializable{

    private Logger logger = LoggerFactory.getLogger(DaonAnalyzer4.class);

    private KeywordSeqFST fst;

    private Model model;

    private static int[] EMPTY_SEQ = new int[]{0};

    private float maxFreq = 1000000;

    private boolean isDebug = true;

    public DaonAnalyzer4(KeywordSeqFST fst, Model model) throws IOException {

        this.fst = fst;
        this.model = model;
    }


    public void setDebug(boolean debug) {
        isDebug = debug;
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


    public List<EojeolInfo> analyzeText(String text) throws IOException {

        List<EojeolInfo> eojeolInfos = new ArrayList<>();

        String[] eojeols = text.split("\\s");

        int outerPrevSeq = 0;

        //tokenizer results
        for(String eojeol : eojeols) {

            EojeolInfo info = new EojeolInfo();

            List<CandidateTerm> terms = new ArrayList<>();

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

            info.setEojeol(eojeol);
            info.setTerms(terms);

            eojeolInfos.add(info);

        }

        return eojeolInfos;
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

                if(isDebug) {
                    //add unknown word
                    logger.info("unkown starIdx : {}, length : {}, str : {}", offset, length, word);
                }

                Keyword keyword = new Keyword(word, POSTag.NNG);
                List<Keyword> keywords = new ArrayList<>(1);
                keywords.add(keyword);

                ExplainInfo explainInfo = new ExplainInfo();
                explainInfo.setMatchInfo(explainInfo.createUnknownMatchInfo());

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


                find(prevSeq, queue, i, ref, eojeolResults);

                CandidateResult first = queue.first();

                if(isDebug) {
                    queue.stream().limit(5).forEach(r -> logger.info("result : {}", r));
                }

                i += (first.getLength() - 1);

                for(CandidateTerm term : first.getTerms()){

                    results.put(term.getOffset(), term);

                }
            }

        }

    }

    private void find(int prevSeq, TreeSet<CandidateResult> queue, int offset, List<Word> ref, TreeMap<Integer, List<Word>> eojeolResults) {

        for (Word w : ref) {

            int seq = w.getHeadSeq();
            int length = w.getLength();

            CandidateResult candidateResult = new CandidateResult();

            List<Keyword> keywords = w.getKeywords();

            ExplainInfo explainInfo = new ExplainInfo();

            if(prevSeq > 0){

                boolean isOuter = false;
                Float cnt = null;
                if(offset == 0){
                    cnt = findOuterSeq(prevSeq, seq);
                    isOuter = true;
                }else{
                    cnt = findInnerSeq(prevSeq, seq);
                }

                if(cnt != null) {

                    //TODO 기존 dictionary 스코어도 가져가야함. 합산
                    explainInfo.setMatchInfo(explainInfo.createPrevMatchInfo(prevSeq, seq, isOuter));
                    explainInfo.setFreqScore(cnt + w.getFreq() + 0.5f); //연결 가중치
                    explainInfo.setTagScore(getTagScore(prevSeq, seq));

                }else{

                    explainInfo.setMatchInfo(explainInfo.createDictionaryMatchInfo(w.getSeq()));
                    explainInfo.setFreqScore(w.getFreq());
                    explainInfo.setTagScore(getTagScore(seq)); // TODO 여러개 매칭된 경우 처리
                }
            }else{

                explainInfo.setMatchInfo(explainInfo.createDictionaryMatchInfo(w.getSeq()));
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
                        nextExplainInfo.setMatchInfo(explainInfo.createNextMatchInfo(seq, nextSeq));
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
                    explainInfo.setMatchInfo(explainInfo.createNextMatchInfo(seq, nextSeq));;
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

            Object outputs = null;
            int lastIdx = offset;

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
                    outputs = fst.getOutputs().add(output, arc.nextFinalOutput);
                    lastIdx = i;
                }


            }

            if(outputs != null){

                PairOutputs<Long,IntsRef> _output = new PairOutputs<>(
                    PositiveIntOutputs.getSingleton(), // word weight
                    IntSequenceOutputs.getSingleton()  // connection wordId's
                );

                ListOfOutputs<PairOutputs.Pair<Long,IntsRef>> fstOutput = new ListOfOutputs<>(_output);

                List<PairOutputs.Pair<Long,IntsRef>> list = fstOutput.asList(outputs);

//                    logger.info("list : {}", list);

//                    List<Pair<Long,IntsRef>> outputList = output.asList(output);

                //표층형 단어
                final String word = new String(chars, offset, (lastIdx + 1));

                final int length = (lastIdx + 1);

                //위치에 따라 불가능한 형태소는 제거 필요

                //디버깅용 로깅
                if(isDebug) {
                    logger.info(" {} find cnt : ({}) : ", word, list.size());

                    list.sort((p1, p2) -> p2.output1.compareTo(p1.output1));

                    for (PairOutputs.Pair<Long, IntsRef> pair : list) {
                        List sb = new ArrayList();

                        IntStream.of(pair.output2.ints).forEach(seq -> {

                            Keyword k = getKeyword(seq);

                            sb.add(k);
                        });

                        logger.info("freq : {}, keywords : {}", pair.output1, sb);
//                        logger.info("pair output1 : {}, output2 : {}", pair.output1, pair.output2);
                    }

                }

                //복합 키워드끼리 짤라서 로깅해야될듯

                addResults(results, offset, length, word, list);

                offset += lastIdx;
            }

//            logger.info("next word =========>");
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

                Keyword keyword = getKeyword(i);

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
                    ", keyword=" + model.getDictionaryMap().get(seq) +
                    ", length=" + length +
                    '}';
        }
    }


    public Keyword getKeyword(int seq){

        daon.analysis.ko.proto.Keyword k = model.getDictionaryMap().get(seq);

        if(k == null){
            return null;
        }else{

            Keyword r = new Keyword();
            r.setSeq(k.getSeq());
            r.setWord(k.getWord());
            r.setTag(POSTag.valueOf(k.getTag()));
            r.setTf(k.getTf());

            return r;
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

        String key = outerPrevSeq + "|" + seq;

        Float cnt = model.getOuterMap().get(key.hashCode());

        return cnt;
    }

    private Float findInnerSeq(int seq, int nSeq){

        String key = seq + "|" + nSeq;

        Float cnt = model.getInnerMap().get(key.hashCode());

        return cnt;
    }


    private float getTagScore(int seq, int nSeq){

        float score = 0;


        daon.analysis.ko.proto.Keyword keyword = model.getDictionaryMap().get(seq);
        daon.analysis.ko.proto.Keyword nKeyword = model.getDictionaryMap().get(nSeq);

        if(keyword != null && nKeyword != null){
            String tag = keyword.getTag();
            String nTag = nKeyword.getTag();

            String key = tag + "|" + nTag;

            Float freq = model.getTagTransMap().get(key.hashCode());

            if(freq != null) {
                score = freq;
            }
        }

        return score;
    }


    private float getTagScore(int seq){

        float score = 0;

        daon.analysis.ko.proto.Keyword keyword = model.getDictionaryMap().get(seq);

        if(keyword != null){
            Integer key = keyword.getTag().hashCode();

            Float freq = model.getTagsMap().get(key);

            if(freq != null) {
                score = freq / 100;
            }
        }

        return score;
    }

}

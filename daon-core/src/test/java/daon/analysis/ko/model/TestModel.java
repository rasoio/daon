package daon.analysis.ko.model;

import daon.analysis.ko.config.POSTag;
import daon.analysis.ko.fst.KeywordFST;
import daon.analysis.ko.reader.JsonFileReader;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.IntSequenceOutputs;
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
public class TestModel {

    private Logger logger = LoggerFactory.getLogger(TestModel.class);

    private KeywordFST fst;

    private Map<Integer, Keyword> dictionary = new HashMap<>();

    private Map<ImmutableTriple, Long> innerInfoMap;

    private Map<ImmutablePair, Long> outerInfoMap;

    private List<KeywordSeq> keywordSeqs;


    private Map<String, Long> tagsMap;
    private Map<ImmutablePair, Long> tagTransMap;


    @Before
    public void before() throws IOException {
        JsonFileReader reader = new JsonFileReader();

        StopWatch watch = new StopWatch();

        watch.start();

        List<Keyword> keywords = reader.read("/Users/mac/work/corpus/model/words.json", Keyword.class);
        List<IrrWord> irrWords = reader.read("/Users/mac/work/corpus/model/irr_words.json", IrrWord.class);

        List<InnerInfo> innerInfos = reader.read("/Users/mac/work/corpus/model/inner_info.json", InnerInfo.class);
        List<OuterInfo> outerInfos = reader.read("/Users/mac/work/corpus/model/outer_info.json", OuterInfo.class);


        List<Tag> tags = reader.read("/Users/mac/work/corpus/model/tags.json", Tag.class);
        List<TagTran> tagTrans = reader.read("/Users/mac/work/corpus/model/tag_trans.json", TagTran.class);

        keywordSeqs = new ArrayList<>();

        keywords.forEach(keyword -> {
            String word = keyword.getWord();
            int seq = keyword.getSeq();

            KeywordSeq keywordSeq = new KeywordSeq(word, seq);
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
        IntSequenceOutputs fstOutput = IntSequenceOutputs.getSingleton();
        Builder<IntsRef> fstBuilder = new Builder<>(FST.INPUT_TYPE.BYTE4, fstOutput);

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

            IntsRef output = fstData.get(input);

            if (output != null) {
                curOutput.copyInts(output);
            }

            IntStream.of(seqs).forEach(curOutput::append);

//            curOutput.append(idx);

            output = curOutput.get();

            //fst 추가, output 사용이 애매..
            fstData.put(input, output);

            keyword.clearInput();
        }

        for (Map.Entry<IntsRef, IntsRef> e : fstData.entrySet()) {
            fstBuilder.add(e.getKey(), e.getValue());
        }


        fst = new KeywordFST(fstBuilder.finish());

        // ㅜㅜ
        dictionary = keywords.stream().collect(Collectors.toMap(Keyword::getSeq, Function.identity()));
//        keywords.stream().forEach(
//            k -> {
//                dictionary.put(k.getSeq(), k);
//            }
//        );

        innerInfoMap = new HashMap<>(innerInfos.size());

        innerInfos.forEach(inner -> {

            ImmutableTriple key = new ImmutableTriple(inner.getpInnerSeq(), inner.getWordSeq(), inner.getnInnerSeq());
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
        String test = "a.5kg 다우니운동화 나이키운동화아디다스 ......남자지갑♧ 아이폰6s 10,000원 [아디다스] 슈퍼스타/스탠스미스 BEST 17종(C77124외)";
//        String test = "사람이 으레 하게";
//        String test = "평화를 목숨처럼 여기는 우주 방위대 마인드C X 호조 작가의 최강 콜라보 파랗고 사랑스러운 녀석들이 매주 금요일 심쿵을 예고한다.";
//        String test = "하지만 질투하는 마음도 있거든요.";
//        String test = "보여지므로";
//        String test = "선생님은 쟝의 소식을 모른다고 하지만 저는 그렇게 생각하지 않아요.";
//        String test = "아버지가방에들어가신다";
//        String test = "1가A나다ABC라마바ABC";
//        String test = "사람이라는 느낌";
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
//            findConnection(outerPrevSeq, eojeolLength, eojeolResults, results);

            results.entrySet().forEach(e->{
                terms.add(e.getValue());
            });

            Map.Entry<Integer, Term> e = results.lastEntry();

            if(e != null) {
                //last word seq
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


                if(i==0) {
                    //output connection 찾기.. 의미 없을수도.. 확률이 높은 경우만 사용할지...??
                    findOuterConnection(outerPrevSeq, ref, eojeolResults);
                }

                logger.info("idx : {}", i);

                i = findInnerConnection(i, ref, eojeolResults, results, -1);

//                logger.info("results : {}", results);

            }
        }
    }

    private void findOuterConnection(int outerPrevSeq, List<Word> ref, TreeMap<Integer, List<Word>> eojeolResults) {
        if (outerPrevSeq > 0) {
            for (Word w : ref) {
                int seq = w.getSeq();

                Long cnt = findOuterSeq(outerPrevSeq, seq);

                if (cnt != null) {
                    Keyword keyword = dictionary.get(seq);
                    Keyword prevKeyword = dictionary.get(outerPrevSeq);

                    logger.info("find outer ==> prev keyword : {}, cur keyword : {}, cnt : {}", prevKeyword, keyword, cnt);
                }
            }
        }
    }

    private int findInnerConnection(int offset, List<Word> ref, TreeMap<Integer, List<Word>> eojeolResults, Map<Integer, Term> results, int innerPrevSeq) {

        int finalLastLength = offset;

        int findLastLength = 0;
        long maxtf = 0;
        int maxLength = 0;
        int maxseq = 0;

        for (Word w : ref) {

            int seq = w.getSeq();
            int length = w.getLength();

            final int nextOffset = offset + length;
            List<Word> nref = eojeolResults.get(nextOffset);

            //연결 확인이 가능할 경우
            if (nref != null) {

                for (Word nw : nref) {

                    int nextSeq = nw.getSeq();
                    int nextLength = nw.getLength();

                    Long cnt = findInnerSeq(innerPrevSeq, seq, nextSeq);

                    // 선정 조건 정의 필요
                    // 우선순위
                    // 1. 연결 길이가 가장 긴것
                    // 2. 길이가 똑같은 경우 cnt (노출 확률이 높은것)
                    if (cnt != null) {

                        //nkeyword 에 대해서 inner 연결 조건 체크 유효한 경우만 사용 하도록

                        Keyword prevKeyword = dictionary.get(innerPrevSeq);
                        Keyword keyword = dictionary.get(seq);
                        Keyword nextKeyword = dictionary.get(nextSeq);

                        //추가 연결 확인
                        //적당한 tf ??
                        //연결이 적은것 ??
                        //ㅜㅜㅜㅜ

                        int lastLength = findInnerConnection(nextOffset, seq, nextSeq, eojeolResults, results); // 뽑힌 nextSeq 사용 하도록 수정, 추가 연결 안되는 경우 스킵 되도록 처리

                        logger.info("1 :::::: find inner ==> prev keyword : {}, keyword : {}, next keyword {}, cnt : {}, lastLength : {}", prevKeyword, keyword, nextKeyword, cnt, lastLength);

                        if(lastLength > findLastLength){
                            findLastLength = lastLength;

                            IntStream.range(offset, offset+ length).forEach(i->{
                                results.remove(i);
                            });

                            IntStream.range(nextOffset, nextOffset + nextLength).forEach(i->{
                                results.remove(i);
                            });

                            results.put(offset, new Term(dictionary.get(seq), offset, length));
                            results.put(nextOffset, new Term(dictionary.get(nextSeq), nextOffset, nextLength));
                        }


                    }
                }

//                logger.info("1 :::::: finalLastLength : {}", finalLastLength);

            //확인 불가능
            }


            // 가능 확률 사용을 통한 대체 단어 선정
            // 1. 길이,
            // 2. 품사 전이 확률,
            // 3. 노출 확률 조합.
            // 비율은 테스트 진행하면서.. 음 완벽할순 없음...

//             가장 긴거 사용
            Keyword word = dictionary.get(seq);
            int len = word.getLength();
            long tf = word.getTf();
            if (len > maxLength) {
                maxLength = len;
                maxseq = seq;
            }

        }


        //최대 score 값 선정

        //연결을 못 찾은 경우 처리 방안...
        if(findLastLength == 0 || findLastLength < maxLength){
            Keyword word = dictionary.get(maxseq);
            logger.info("not find inner ==> maxseq : {}, keyword : {}", maxseq, word);

            IntStream.range(offset, offset + maxLength).forEach(i->{
                results.remove(i);
            });

            results.put(offset, new Term(word, offset, word.getLength()));
        }


        Integer k = (Integer)((TreeMap)results).lastKey();

        if(k != null){
            Term v = results.get(k);
            finalLastLength = k + v.getLength() - 1;
        }
        return finalLastLength;
    }



    private int findInnerConnection(int offset, int innerPrevSeq, int seq, TreeMap<Integer, List<Word>> eojeolResults, Map<Integer, Term> results) {

        int finalLastLength = offset;

        Keyword keyword = dictionary.get(seq);
        int length = keyword.getLength();

        final int nextOffset = offset + length;
        List<Word> nref = eojeolResults.get(nextOffset);

        long findCnt = 0;

        //연결 확인이 가능할 경우
        if (nref != null) {
            for (Word nw : nref) {

                int nextSeq = nw.getSeq();
                int nextLength = nw.getLength();

                Long cnt = findInnerSeq(innerPrevSeq, seq, nextSeq);

                if (cnt != null && cnt > findCnt) {

                    findCnt = cnt;

                    //nkeyword 에 대해서 inner 연결 조건 체크 유효한 경우만 사용 하도록

                    Keyword prevKeyword = dictionary.get(innerPrevSeq);
                    Keyword nextKeyword = dictionary.get(nextSeq);


                    IntStream.range(offset, offset+ length).forEach(i->{
                        results.remove(i);
                    });

                    IntStream.range(nextOffset, nextOffset + nextLength).forEach(i->{
                        results.remove(i);
                    });

                    results.put(offset, new Term(dictionary.get(seq), offset, length));
                    results.put(nextOffset, new Term(dictionary.get(nextSeq), nextOffset, nextLength));

                    finalLastLength = nextOffset + nextLength;

                    //추가 연결 확인
                    int lastLength = findInnerConnection(nextOffset, seq, nextSeq, eojeolResults, results); // 뽑힌 nextSeq 사용 하도록 수정, 추가 연결 안되는 경우 스킵 되도록 처리

                    if(lastLength > finalLastLength){
                        finalLastLength = lastLength;
                    }

                    logger.info("2 : find inner ==> prev keyword : {}, keyword : {} ({}, {}), next keyword {} ({}, {}), cnt : {}, lastLength : {}", prevKeyword, keyword, offset, length, nextKeyword, nextOffset, nextLength, cnt, lastLength);
                }
            }

        }

//        logger.info("2: finalLastLength : {}", finalLastLength);


        return finalLastLength;
    }




    private TreeMap<Integer, List<Word>> lookup(char[] chars, int charsLength) throws IOException {

        //offset 별 기분석 사전 Term 추출 결과
        TreeMap<Integer, List<Word>> results = new TreeMap<>();

        final FST.BytesReader fstReader = fst.getBytesReader();

        FST.Arc<IntsRef> arc = new FST.Arc<>();

        int offset = 0;

        for (; offset < charsLength; offset++) {
            arc = fst.getFirstArc(arc);
            IntsRef output = fst.getOutputs().getNoOutput();
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
                    final IntsRef wordSeqs = fst.getOutputs().add(output, arc.nextFinalOutput);

                    //표층형 단어
                    final String word = new String(chars, offset, (i + 1));

                    final int length = (i + 1);

                    //위치에 따라 불가능한 형태소는 제거 필요


                    //debugging
                    List sb = new ArrayList();
                    for(Integer seq : wordSeqs.ints){
                        Keyword k = dictionary.get(seq);
                        sb.add(k);
                    }

                    logger.info(" {} ({}) : {}", word, wordSeqs.ints.length, sb);

                    addResults(results, offset, length, wordSeqs);
                }
            }
        }

        return results;
    }

    class Word {
        private int seq;
        private int length;

        public Word(int seq, int length) {
            this.seq = seq;
            this.length = length;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
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
     * @param wordSeqs
     */
    private void addResults(Map<Integer, List<Word>> results, int offset, int length, IntsRef wordSeqs) {
        List<Word> words = results.get(offset);

        if(words == null){
            words = new ArrayList<>();
        }

        for (int i = 0; i < wordSeqs.length; i++) {
            int idx = wordSeqs.ints[i];


//            KeywordSeq keywordSeq = keywordSeqs.get(idx);
//            words.add(new Word(keywordSeq.getSeqs()[0], length));

            Keyword keyword = dictionary.get(idx);

            if(keyword != null) {
                words.add(new Word(keyword.getSeq(), length));
            }
        }

        results.put(offset, words);
    }


    private Long findOuterSeq(int outerPrevSeq, int seq){

        ImmutablePair key = new ImmutablePair(outerPrevSeq, seq);

        Long cnt = outerInfoMap.get(key);

        return cnt;
    }

    private Long findInnerSeq(int pSeq, int seq, int nSeq){

        ImmutableTriple key = new ImmutableTriple(pSeq, seq, nSeq);

        Long cnt = innerInfoMap.get(key);

        return cnt;
    }
}

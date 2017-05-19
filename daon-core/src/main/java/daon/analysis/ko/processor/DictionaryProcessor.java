package daon.analysis.ko.processor;

import daon.analysis.ko.fst.DaonFST;
import daon.analysis.ko.model.Term;
import daon.analysis.ko.model.ExplainInfo;
import daon.analysis.ko.model.Keyword;
import daon.analysis.ko.model.ModelInfo;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.fst.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mac on 2017. 5. 18..
 */
public class DictionaryProcessor {

    private Logger logger = LoggerFactory.getLogger(DictionaryProcessor.class);

    private ModelInfo modelInfo;

    public static DictionaryProcessor create(ModelInfo modelInfo) {

        return new DictionaryProcessor(modelInfo);
    }

    private DictionaryProcessor(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
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
    public TreeMap<Integer, List<Term>> process(char[] chars, int charsLength) throws IOException {

        DaonFST fst = modelInfo.getFst();

        //offset 별 기분석 사전 Term 추출 결과
        TreeMap<Integer, List<Term>> results = new TreeMap<>();

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

                List<PairOutputs.Pair<Long,IntsRef>> list = fst.asList(outputs);

                //표층형 단어
                final String word = new String(chars, offset, (lastIdx + 1));

                final int length = (lastIdx + 1);

                //위치에 따라 불가능한 형태소는 제거 필요

                //디버깅용 로깅
                if(logger.isDebugEnabled()) {
                    logger.debug(" {} find cnt : ({}) : ", word, list.size());

                    list.sort((p1, p2) -> p2.output1.compareTo(p1.output1));

                    for (PairOutputs.Pair<Long, IntsRef> pair : list) {
                        List sb = new ArrayList();

                        IntStream.of(pair.output2.ints).forEach(seq -> {

                            Keyword k = modelInfo.getKeyword(seq);

                            sb.add(k);
                        });

                        logger.debug("freq : {}, keywords : {}", pair.output1, sb);
                    }

                }

                //복합 키워드끼리 짤라서 로깅해야될듯

                addResults(results, offset, length, word, list);

                offset += lastIdx;
            }

        }

        return results;
    }

    /**
     * 결과에 키워드 term 추가
     * @param results
     * @param offset
     * @param length
     * @param list
     */
    private void addResults(Map<Integer, List<Term>> results, int offset, int length, String surface, List<PairOutputs.Pair<Long,IntsRef>> list) {
        List<Term> terms = results.get(offset);

        if(terms == null){
            terms = new ArrayList<>();
        }

        for(PairOutputs.Pair<Long,IntsRef> pair : list){
            int[] findSeqs = pair.output2.ints;
            long freq = pair.output1;
            List<Keyword> keywords = IntStream.of(findSeqs).mapToObj(i->{

                Keyword keyword = modelInfo.getKeyword(i);

                return keyword;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            ExplainInfo explainInfo = ExplainInfo.create().dictionaryMatch(findSeqs)
                    .freqScore((float)freq / modelInfo.getMaxFreq())
                    .tagScore(getTagScore(findSeqs));
            //어절 분석 사전인 경우 여러 seq에 대한 tagScore 도출 방법..??


            Term term = new Term(offset, length, surface, keywords, explainInfo);

            terms.add(term);
        }

        results.put(offset, terms);
    }

    private float getTagScore(int[] findSeqs){

        float score = 1f;

        if(findSeqs.length == 1){
            return modelInfo.getTagScore(findSeqs[0]);
        }else if(findSeqs.length == 2){
            return modelInfo.getTagScore(findSeqs[0], findSeqs[1]);
        }else{
            return score;
        }
    }
}
